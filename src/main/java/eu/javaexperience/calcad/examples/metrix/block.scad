//Metrix construction kit parts, block generator.
// by Dankó Dávid 2020
//
//All values in mm.

////Generator values for users

//Should the block drilled by the X axis
drill_x = true;
//Should the block drilled by the Y axis
drill_y = true;
//Should the block drilled by the Z axis
drill_z = true;

//Length of the block on X axises in unit (count)
block_x = 10;

//Length of the block on Y axises in unit (count)
block_y = 10;

//Length of the block on Z axises in unit (count)
block_z = 1;

//Use releive? (to save material when printing the object)
relieve = true;

//diameter of the releive holes (when releive used)?
relieve_dia = 2;

relieve_x = drill_x && relieve;
relieve_y = drill_y && relieve;
relieve_z = drill_z && relieve;



////Settings

//drill holes (default: to fit an M4 screw)
hole_dia = 4.2;

//number of faces while rendering object
$fn= 25;



////Calcualted values
hole_radius = hole_dia/2;
relieve_radius = relieve_dia/2;
releive_offset = 2.5 + relieve_radius/2;

////modules
module drill_hole(h=12)
{
    cylinder(h, r1= hole_radius, r2= hole_radius);
}

module releive_hole(h=12)
{
    cylinder(h, r1= relieve_radius, r2= relieve_radius);
}

function first_last(i, max) = 0 < i && i< max;

module relieve_holes(rotate, x0, y0, z0, h=12, fl_x, fl_y)
{
    if(fl_x)
    {
        rotate(rotate) translate([x0+5, y0, z0]) releive_hole(h);
    }
    
    if(fl_y)
    {
        rotate(rotate) translate([x0, y0+5, z0]) releive_hole(h);
    }
    
    //near side holes
    rotate(rotate) translate([x0+releive_offset, y0+releive_offset, z0]) releive_hole(h);
    rotate(rotate) translate([x0+releive_offset, y0-releive_offset, z0]) releive_hole(h);
    
    //far side holes
    rotate(rotate) translate([x0-releive_offset, y0+releive_offset, z0]) releive_hole(h);
    rotate(rotate) translate([x0-releive_offset, y0-releive_offset, z0]) releive_hole(h);
}

module machining(rotate, x0, y0, z0, drill, fl_x, fl_y)
{
    rotate(rotate) translate([x0, y0, z0]) drill_hole(drill);
    relieve_holes(rotate, x0, y0, z0, drill, fl_x, fl_y);
}

module main()
{
difference
//union //for debug
()
    {
        //main bar cube
        cube([block_x*10, block_y*10, block_z*10], false);
    
    if(drill_x)
    {
        for(y = [1:block_y])
        {
            for(z = [1:block_z])
            {
                machining
                (
                    [90, 0, 90],
                    -5+y*10,
                    -5+z*10,
                    -0.5, 1+block_x*10,
                    first_last(y, block_y),
                    first_last(z, block_z)
                );
            }
        }
    }
    
    if(drill_y)
    {
        for(x = [1:block_x])
        {
            for(z = [1:block_z])
            {
                machining
                (
                    [90, 0, 0],
                    -5+x*10,
                    -5+z*10,
                    -0.5-block_y*10,
                    1+block_y*10,
                    first_last(x, block_x),
                    first_last(z, block_z)
                );
            }
        }
    }
    
    if(drill_z)
    {       
        for(x = [1:block_x])
        {
            for(y = [1:block_y])
            {
                machining
                (
                    [0, 0, 0],
                    -5+x*10,
                    -5+y*10,
                    -0.5, 1+block_z*10,
                    first_last(x, block_x),
                    first_last(y, block_y)
                );
            }
        }
    }
}
}

////Rendering
main();

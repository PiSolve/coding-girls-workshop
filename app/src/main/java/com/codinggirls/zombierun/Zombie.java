package com.xu.liferpg;

import com.google.android.gms.maps.model.LatLng;

/**
 * Some of this stuff may be unneccessary. cut as you see fit Renu
 */

public class Zombie {

    LatLng location;
    int ID;
    int speed;

    public Zombie( LatLng newloc, int newID, int newspeed){
        this.location=newloc;
        this.ID=newID;
        this.speed=newspeed;
    }
    public void setLocation(LatLng newloc){
        this.location=newloc;

    }
    public LatLng getLocation(){
        return this.location;

    }
    public void setID( int newID){
        this.ID=newID;
    }
    public int getID(){
        return this.ID;
    }

    public void setSpeed(int newSpeed){
        this.speed=newSpeed;
    }

    public int getSpeed(){
        return this.speed;
    }
}

package com.xu.liferpg;

import com.google.android.gms.maps.model.LatLng;

/**
 * Some of this stuff may be unneccessary. cut as you see fit Renu
 */

public class Zombie {

    LatLng location;
    int ID;
    float speed;

    public Zombie( LatLng newloc, int newID, float newspeed){
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

    public void setSpeed(float newSpeed){
        this.speed=newSpeed;
    }

    public float getSpeed(){
        return this.speed;
    }
}

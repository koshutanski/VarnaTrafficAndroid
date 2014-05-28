package VarnaTraffic.Helpers;

import android.location.Location;

/**
 * Date: 9/18/13
 */
public class LiveDataModel {
   public String  ArriveIn;
   public String ArriveTime;
   public String Delay;
   public String Device;
   public Integer Direction;
   public String DistanceLeft;
   public String Line;
   public Location Position;
   public Integer State;

    public LiveDataModel()
    {  }


    public LiveDataModel(String arriveIn, String arriveTime, String delay, String device, Integer  direction, String distanceLeft,String line, Location position, Integer state)
    {
      ArriveIn = arriveIn;
        ArriveTime = arriveTime;
        Delay = delay;
        Device = device;
        Direction = direction;
        DistanceLeft = distanceLeft;
        Line = line;
        Position = position;
        State=state;
    }

}

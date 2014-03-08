package VarnaTraffic.Helpers;

/**
 * Date: 9/18/13
 */
public class ScheduleModel {
    public String DestStationTime;
    public String Device;
    public String Text;

    public ScheduleModel(){}

    public ScheduleModel(String destStationTime, String device,String text){
        DestStationTime = destStationTime;
        Device = device;
        Text = text;
    }
}

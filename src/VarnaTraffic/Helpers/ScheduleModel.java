package VarnaTraffic.Helpers;

/**
 * Created with IntelliJ IDEA.
 * User: miroslav
 * Date: 9/18/13
 * Time: 10:42 AM
 * To change this template use File | Settings | File Templates.
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

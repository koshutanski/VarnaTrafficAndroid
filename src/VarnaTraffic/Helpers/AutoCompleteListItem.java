package VarnaTraffic.Helpers;

import java.io.Serializable;

/**
 * Created with IntelliJ IDEA.
 * User: miroslav
 * Date: 9/17/13
 * Time: 11:20 AM
 * To change this template use File | Settings | File Templates.
 */
public class AutoCompleteListItem implements Serializable {
    public AutoCompleteListItem(){

    }
    public AutoCompleteListItem(int id, String text)
    {
        Id = id;
        Text = text;
    }

    public String getText()
    {
        return Text;
    }

    public Integer getId()
    {
        return Id;
    }


    @Override
    public String toString() {
        return this.Text;
    }


    public Integer Id;
    public String Text;
}

package VarnaTraffic.Helpers;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

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

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Id", Id);
            jsonObject.put("Text", Text);

            return jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
            return "";
        }
    }

    public void serializeJSON(String jsonListItem) {

        try {
            JSONObject jsonObject = new JSONObject(jsonListItem);
            Id= jsonObject.getInt("Id");
            Text = jsonObject.getString("Text");

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}

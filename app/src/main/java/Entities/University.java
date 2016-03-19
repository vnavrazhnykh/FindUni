package Entities;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;

import Services.ImageService;

/**
 * Created by yudzh_000 on 17.03.2016.
 */
public class University implements Serializable, Entity {
    int id;

    String name;
    String address;
    String phone;
    String site;
    Town town;
    int liked;
    double meanPrice;
    double meanPoints;
    String logoPath;
    String imagePath;
    int loaded;

    private boolean loadSpecialities = false;

    public University() {
    }

    public University(String name, String address, String phone, String site, Town town,
                      double meanPrice, int meanPoints) {
        this.name = name;
        this.address = address;
        this.town = town;
        this.phone = phone;
        this.site = site;
        this.meanPrice = meanPrice;
        this.meanPoints = meanPoints;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    public String getSite() {
        return site;
    }


    public double getMeanPrice() {
        return meanPrice;
    }

    public double getMeanPoints() {
        return meanPoints;
    }



    public int getId() {
        return id;
    }

    /**
     * Put obj to bd if there is no with this name
     */
    public void put(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("Select * from University Where name = ?", new String[]{name});
        if (cursor.moveToFirst()) {
            this.id = cursor.getInt(0);
        } else {
            ContentValues values = new ContentValues();
            values.put("name", name);
            values.put("address", address);
            values.put("town_id", town.getId());
            values.put("phone", phone);
            values.put("site", site);
            values.put("is_favourite", liked);
            values.put("is_loaded", 0);
            values.put("mean_points", meanPoints);
            values.put("mean_price", meanPrice);
            values.put("logo_path", logoPath);
            values.put("image_path", imagePath);
            db.insert("University", null, values);
            cursor = db.rawQuery("Select * from Town Where name =?", new String[]{name});
            if (cursor.moveToFirst()) {
                this.id = cursor.getInt(0);
            }

        }
    }

    /**
     * Load obj from Local BD if there is no obj with this name put it in BD
     * @param name Name of Universoty
     * @return The obj from BD
     */
    public static University getOrInsert(String name) {
        // TODO: 17.03.2016 Load Obj from Local BD if no put in
        return null;
    }


    /**
     * Load Specialities from BD if it is no loaded before.
     */
    public void loadSpecialities() {
        if(!loadSpecialities) {

        }
    }


    public Town getTown() {
        return town;
    }

    public int getLiked() {
        return liked;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public String getImagePath() {
        return imagePath;
    }

    public int getLoaded() {
        return loaded;
    }

    public static University initFromCursor(Cursor cursor, Town town) {
        University university = new University();

        university.id = cursor.getInt(0);
        university.name = cursor.getString(1);
        university.town = town;
        university.meanPrice = cursor.getInt(3);
        university.meanPoints = cursor.getInt(4);
        university.imagePath = cursor.getString(5);
        university.logoPath = cursor.getString(6);
        university.loaded = cursor.getInt(7);
        university.liked = cursor.getInt(8);
        university.site = cursor.getString(9);
        university.address = cursor.getString(10);
        university.phone = cursor.getString(11);
        return university; // TODO: 17.03.2016
    }

    public static University initFromJSON(JSONObject json, Town town, AppCompatActivity activity) throws JSONException, IOException {
        University university = new University();
        university.town = town;
        university.name = json.getString("name");
        university.address = json.getString("address");
        university.logoPath = ImageService.saveToFileFromUrl(activity.getApplicationContext(),
                json.getString("logo_url"));
        university.imagePath = ImageService.saveToFileFromUrl(activity.getApplicationContext(), json.getString("image_url"));
        double meanPoints, meanPrice;

        if(json.isNull("mean_point")) {
            meanPoints = 0;
        } else {
            meanPoints = json.getDouble("mean_point");
        }
        if(json.isNull("mean_price")) {
            meanPrice = 0;
        } else {
            meanPrice = json.getDouble("mean_price");
        }
        university.meanPoints = meanPoints;
        university.meanPrice = meanPrice;
        university.site = json.getString("site_url");
        university.phone = json.getString("telephone");
        university.town = town;
        university.liked = 0;
        return university;
    }
}
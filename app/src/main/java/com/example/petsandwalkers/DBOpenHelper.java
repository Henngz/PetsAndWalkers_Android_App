package com.example.petsandwalkers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DBOpenHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;


    public DBOpenHelper(Context context){
        super(context,"db_pets_walking",null,1);
        db = getReadableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        db.execSQL("CREATE TABLE IF NOT EXISTS user_info(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT," +
                "password TEXT," +
                "email TEXT," +
                "phone TEXT," +
                "create_time DATETIME," +
                "update_time DATETIME)");

        db.execSQL("CREATE TABLE IF NOT EXISTS account_info(" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "username TEXT," +
                "identity TEXT," +
                "service_time_range TEXT," +
                "service_location TEXT," +
                "price TEXT," +
                "latitude REAL," +
                "longitude REAL," +
                "phone_number TEXT," +
                "email_address TEXT," +
                "additional_info TEXT)");
    }
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL("DROP TABLE IF EXISTS user_info");
        db.execSQL("DROP TABLE IF EXISTS account_info");
        onCreate(db);
    }

    public Boolean insertData(String username,String password,String email,String phone,String create_time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("username", username);
        contentValues.put("password", password);
        contentValues.put("email", email);
        contentValues.put("phone", phone);
        contentValues.put("create_time", create_time);

        long result = db.insert("user_info", null, contentValues);

        if(result == -1){
            return false;
        }else {
            return true;
        }
    }

    /*
        This method used to check the username.
     */
    public Boolean checkUsername(String username){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from user_info where username = ?", new String[] {username});

        if(cursor.getCount() > 0){
            return true;
        }else {
            return false;
        }
    }

    /*
        This method used to check the password.
     */
    public Boolean checkUsernamePassword(String username, String password){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("select * from user_info where username = ? " +
                "and password = ?", new String[] {username, password});

        if(cursor.getCount() > 0){
            return true;
        }else {
            return false;
        }
    }

    public void delete(String username,String password){
        db.execSQL("DELETE FROM user_info WHERE username = AND password ="+username+password);
    }

    public Boolean updatePassword(String username, String password, String update_time){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("password", password);
        contentValues.put("update_time", update_time);

        long result = db.update("user_info", contentValues, "username = ?", new String[]{username});

        if(result == -1){
            return false;
        }else {
            return true;
        }
    }

    public void updateEmail(String email){
        db.execSQL("UPDATE user_info SET email = ?, update_time = time",new Object[]{email});
    }

    public void updatePhone(String phone){
        db.execSQL("UPDATE user_info SET phone = ?, update_time = time",new Object[]{phone});
    }

    public boolean insertAccountInfo(String username, String identity, String serviceTimeRange, String serviceLocation,
                                     String price, double latitude, double longitude, String phoneNumber,
                                     String emailAddress, String additionalInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("username", username);
        contentValues.put("identity", identity);
        contentValues.put("service_time_range", serviceTimeRange);
        contentValues.put("service_location", serviceLocation);
        contentValues.put("price", price);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("phone_number", phoneNumber);
        contentValues.put("email_address", emailAddress);
        contentValues.put("additional_info", additionalInfo);

        long result = db.insert("account_info", null, contentValues);

        return result != -1;
    }

    public Cursor getAccountInfo(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account_info WHERE username = ?", new String[]{username});
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public boolean updateAccountInfo(String username, String identity, String serviceTimeRange, String serviceLocation,
                                     String price, double latitude, double longitude, String phoneNumber,
                                     String emailAddress, String additionalInfo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put("identity", identity);
        contentValues.put("service_time_range", serviceTimeRange);
        contentValues.put("service_location", serviceLocation);
        contentValues.put("price", price);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("phone_number", phoneNumber);
        contentValues.put("email_address", emailAddress);
        contentValues.put("additional_info", additionalInfo);

        int result = db.update("account_info", contentValues, "username = ?", new String[]{username});

        Log.d("DB_UPDATE", "Update result: " + result + " for username: " + username);

        return result > 0;
    }

    public List<PetWalker> getUsersByType(String userType) {
        List<PetWalker> petWalkerList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account_info WHERE identity = ?", new String[]{userType});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex("_id");
                int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;

                int usernameIndex = cursor.getColumnIndex("username");
                String username = usernameIndex != -1 ? cursor.getString(usernameIndex) : "";

                int identityIndex = cursor.getColumnIndex("identity");
                String identity = identityIndex != -1 ? cursor.getString(identityIndex) : "";

                int latitudeIndex = cursor.getColumnIndex("latitude");
                double latitude = latitudeIndex != -1 ? cursor.getDouble(latitudeIndex) : 0;

                int longitudeIndex = cursor.getColumnIndex("longitude");
                double longitude = longitudeIndex != -1 ? cursor.getDouble(longitudeIndex) : 0;

                int serviceTimeRangeIndex = cursor.getColumnIndex("service_time_range");
                String serviceTimeRange = serviceTimeRangeIndex != -1 ? cursor.getString(serviceTimeRangeIndex) : "";

                int serviceLocationIndex = cursor.getColumnIndex("service_location");
                String serviceLocation = serviceLocationIndex != -1 ? cursor.getString(serviceLocationIndex) : "";

                int priceIndex = cursor.getColumnIndex("price");
                String price = priceIndex != -1 ? cursor.getString(priceIndex) : "";

                int phoneNumberIndex = cursor.getColumnIndex("phone_number");
                String phoneNumber = phoneNumberIndex != -1 ? cursor.getString(phoneNumberIndex) : "";

                int emailAddressIndex = cursor.getColumnIndex("email_address");
                String emailAddress = emailAddressIndex != -1 ? cursor.getString(emailAddressIndex) : "";

                int additionalInfoIndex = cursor.getColumnIndex("additional_info");
                String additionalInfo = additionalInfoIndex != -1 ? cursor.getString(additionalInfoIndex) : "";

                petWalkerList.add(new PetWalker(id, username, identity, latitude, longitude,
                        serviceTimeRange, serviceLocation, price, phoneNumber, emailAddress, additionalInfo));

                Log.d("DBOpenHelper", "Username: " + username + ", Identity: " + identity);

            }
            cursor.close();

            Log.d("DBOpenHelper", "Filter Option: " + userType);

        }

        return petWalkerList;
    }

    public List<PetWalker> getAllPetWalkers() {
        List<PetWalker> petWalkers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM account_info", null);

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex("_id");
                int id = idIndex != -1 ? cursor.getInt(idIndex) : 0;

                int usernameIndex = cursor.getColumnIndex("username");
                String username = usernameIndex != -1 ? cursor.getString(usernameIndex) : "";

                int identityIndex = cursor.getColumnIndex("identity");
                String identity = identityIndex != -1 ? cursor.getString(identityIndex) : "";

                int latitudeIndex = cursor.getColumnIndex("latitude");
                double latitude = latitudeIndex != -1 ? cursor.getDouble(latitudeIndex) : 0;

                int longitudeIndex = cursor.getColumnIndex("longitude");
                double longitude = longitudeIndex != -1 ? cursor.getDouble(longitudeIndex) : 0;

                int serviceTimeRangeIndex = cursor.getColumnIndex("service_time_range");
                String serviceTimeRange = serviceTimeRangeIndex != -1 ? cursor.getString(serviceTimeRangeIndex) : "";

                int serviceLocationIndex = cursor.getColumnIndex("service_location");
                String serviceLocation = serviceLocationIndex != -1 ? cursor.getString(serviceLocationIndex) : "";

                int priceIndex = cursor.getColumnIndex("price");
                String price = priceIndex != -1 ? cursor.getString(priceIndex) : "";

                int phoneNumberIndex = cursor.getColumnIndex("phone_number");
                String phoneNumber = phoneNumberIndex != -1 ? cursor.getString(phoneNumberIndex) : "";

                int emailAddressIndex = cursor.getColumnIndex("email_address");
                String emailAddress = emailAddressIndex != -1 ? cursor.getString(emailAddressIndex) : "";

                int additionalInfoIndex = cursor.getColumnIndex("additional_info");
                String additionalInfo = additionalInfoIndex != -1 ? cursor.getString(additionalInfoIndex) : "";

                PetWalker petWalker = new PetWalker(id, username, identity, latitude, longitude, serviceTimeRange, serviceLocation, price ,phoneNumber, emailAddress, additionalInfo);
                petWalkers.add(petWalker);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return petWalkers;
    }

}

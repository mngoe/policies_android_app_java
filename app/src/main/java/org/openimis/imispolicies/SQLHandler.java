//Copyright (c) 2016-%CurrentYear% Swiss Agency for Development and Cooperation (SDC)
//
//The program users must agree to the following terms:
//
//Copyright notices
//This program is free software: you can redistribute it and/or modify it under the terms of the GNU AGPL v3 License as published by the 
//Free Software Foundation, version 3 of the License.
//This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of 
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU AGPL v3 License for more details www.gnu.org.
//
//Disclaimer of Warranty
//There is no warranty for the program, to the extent permitted by applicable law; except when otherwise stated in writing the copyright 
//holders and/or other parties provide the program "as is" without warranty of any kind, either expressed or implied, including, but not 
//limited to, the implied warranties of merchantability and fitness for a particular purpose. The entire risk as to the quality and 
//performance of the program is with you. Should the program prove defective, you assume the cost of all necessary servicing, repair or correction.
//
//Limitation of Liability 
//In no event unless required by applicable law or agreed to in writing will any copyright holder, or any other party who modifies and/or 
//conveys the program as permitted above, be liable to you for damages, including any general, special, incidental or consequential damages 
//arising out of the use or inability to use the program (including but not limited to loss of data or data being rendered inaccurate or losses 
//sustained by you or third parties or a failure of the program to operate with any other programs), even if such holder or other party has been 
//advised of the possibility of such damages.
//
//In case of dispute arising out or in relation to the use of the program, it is subject to the public law of Switzerland. The place of jurisdiction is Berne.

package org.openimis.imispolicies;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.NonNull;
import android.text.TextUtils;

import org.intellij.lang.annotations.Language;
import org.openimis.imispolicies.tools.Log;
import android.util.Xml;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Objects;

public class SQLHandler extends SQLiteOpenHelper {

    public static final String DBNAME = "IMIS.db3";
    private static final String OFFLINEDBNAME = "ImisData.db3";
    public Boolean isPrivate = true;
    private final Context context;
    private final Global global;
    private SQLiteDatabase mDatabase;
    private static final int DATABASE_VERSION = 6;

    private boolean doesColumnExist(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info('" + tableName + "')", null);
        boolean exists = false;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndex("name")).equals(columnName)) {
                    exists = true;
                    break;
                }
            }
            cursor.close();
        }
        return exists;
    }

    //table names
    private static final String android_metadata = "android_metadata";
    private static final String sqlite_sequence = "sqlite_sequence";
    public static final String tblConfirmationTypes = "tblConfirmationTypes";
    public static final String tblControlNumber = "tblControlNumber";
    public static final String tblControls = "tblControls";
    public static final String tblEducations = "tblEducations";
    public static final String tblFamilies = "tblFamilies";
    public static final String tblFamilyTypes = "tblFamilyTypes";
    public static final String tblFeedbacks = "tblFeedbacks";
    public static final String tblGender = "tblGender";
    public static final String tblHF = "tblHF";
    public static final String tblIMISDefaultsPhone = "tblIMISDefaultsPhone";
    public static final String tblIdentificationTypes = "tblIdentificationTypes";
    public static final String tblInsuree = "tblInsuree";
    public static final String tblInsureePolicy = "tblInsureePolicy";
    public static final String tblLanguages = "tblLanguages";
    public static final String tblLocations = "tblLocations";
    public static final String tblOfficer = "tblOfficer";
    public static final String tblPayer = "tblPayer";
    public static final String tblPolicy = "tblPolicy";
    public static final String tblPremium = "tblPremium";
    public static final String tblProduct = "tblProduct";
    public static final String tblProfessions = "tblProfessions";
    public static final String tblRecordedPolicies = "tblRecordedPolicies";
    public static final String tblRelations = "tblRelations";
    public static final String tblRenewals = "tblRenewals";
    public static final String tblBulkControlNumbers = "tblBulkControlNumbers";
    public static final String tblFamilySMS = "tblFamilySMS";
    public static final String tblIncomeLevel = "tblIncomeLevel";
    public static final String tblContributionPlan = "tblContributionPlan";
    public static final String tblInsureeAttachments = "tblInsureeAttachments";

    public SQLHandler(Context context) {
        super(context, DBNAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
        global = (Global) this.context.getApplicationContext();
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("SQLHandler", "Starting database upgrade from version " + oldVersion + " to " + newVersion);
        
        // Gérer les différentes versions
        if (oldVersion < 2) {
            Log.d("SQLHandler", "Upgrading from version " + oldVersion + " to 2");
            
            // Vérifier et ajouter les colonnes une par une
            String[] newColumns = {"Disability", "DisablingDisease", "CoverageInsurance", "HouseType", "ResidencePlace"};
            for (String column : newColumns) {
                boolean exists = doesColumnExist(db, tblInsuree, column);
                Log.d("SQLHandler", "Checking column " + column + " exists: " + exists);
                
                if (!exists) {
                    String alterSQL = "ALTER TABLE " + tblInsuree + " ADD COLUMN " + column + " INTEGER;";
                    Log.d("SQLHandler", "Adding column: " + alterSQL);
                    try {
                        db.execSQL(alterSQL);
                        Log.d("SQLHandler", "Column " + column + " added successfully");
                    } catch (Exception e) {
                        Log.e("SQLHandler", "Error adding column " + column + ": " + e.getMessage(), e);
                        throw e; // Rethrow to ensure the upgrade fails if a column addition fails
                    }
                }
            }
        }

        // Ajouter d'autres modifications de schéma si nécessaire
        if (oldVersion < 3) {
            Log.d("SQLHandler", "Upgrading from version " + oldVersion + " to 3");
            // Ajouter d'autres colonnes ou modifications ici
        }

        Log.d("SQLHandler", "Database upgrade completed successfully from version " + oldVersion + " to " + newVersion);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        try {
            Log.d("SQLHandler", "Starting database creation");

            // Créer les tables système
            Log.d("SQLHandler", "Creating system tables");
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + android_metadata + " (locale TEXT)");
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + sqlite_sequence + " (name TEXT, seq INTEGER)");

            // Créer la table tblInsuree
            Log.d("SQLHandler", "Creating tblInsuree table with new columns");
            String createInsureeTable = "CREATE TABLE IF NOT EXISTS " + tblInsuree + " (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "CHFID TEXT," +
                    "OtherNames TEXT," +
                    "LastName TEXT," +
                    "OtherNames1 TEXT," +
                    "LastName1 TEXT," +
                    "OtherNames2 TEXT," +
                    "LastName2 TEXT," +
                    "OtherNames3 TEXT," +
                    "LastName3 TEXT," +
                    "OtherNames4 TEXT," +
                    "LastName4 TEXT," +
                    "OtherNames5 TEXT," +
                    "LastName5 TEXT," +
                    "OtherNames6 TEXT," +
                    "LastName6 TEXT," +
                    "OtherNames7 TEXT," +
                    "LastName7 TEXT," +
                    "OtherNames8 TEXT," +
                    "LastName8 TEXT," +
                    "OtherNames9 TEXT," +
                    "LastName9 TEXT," +
                    "OtherNames10 TEXT," +
                    "LastName10 TEXT," +
                    "GenderId INTEGER," +
                    "GenderId1 INTEGER," +
                    "GenderId2 INTEGER," +
                    "GenderId3 INTEGER," +
                    "GenderId4 INTEGER," +
                    "GenderId5 INTEGER," +
                    "GenderId6 INTEGER," +
                    "GenderId7 INTEGER," +
                    "GenderId8 INTEGER," +
                    "GenderId9 INTEGER," +
                    "GenderId10 INTEGER," +
                    "DateOfBirth TEXT," +
                    "DateOfBirth1 TEXT," +
                    "DateOfBirth2 TEXT," +
                    "DateOfBirth3 TEXT," +
                    "DateOfBirth4 TEXT," +
                    "DateOfBirth5 TEXT," +
                    "DateOfBirth6 TEXT," +
                    "DateOfBirth7 TEXT," +
                    "DateOfBirth8 TEXT," +
                    "DateOfBirth9 TEXT," +
                    "DateOfBirth10 TEXT," +
                    "Photo TEXT," +
                    "Photo1 TEXT," +
                    "Photo2 TEXT," +
                    "Photo3 TEXT," +
                    "Photo4 TEXT," +
                    "Photo5 TEXT," +
                    "Photo6 TEXT," +
                    "Photo7 TEXT," +
                    "Photo8 TEXT," +
                    "Photo9 TEXT," +
                    "Photo10 TEXT," +
                    "MaritalStatusId INTEGER," +
                    "MaritalStatusId1 INTEGER," +
                    "MaritalStatusId2 INTEGER," +
                    "MaritalStatusId3 INTEGER," +
                    "MaritalStatusId4 INTEGER," +
                    "MaritalStatusId5 INTEGER," +
                    "MaritalStatusId6 INTEGER," +
                    "MaritalStatusId7 INTEGER," +
                    "MaritalStatusId8 INTEGER," +
                    "MaritalStatusId9 INTEGER," +
                    "MaritalStatusId10 INTEGER," +
                    "EducationId INTEGER," +
                    "EducationId1 INTEGER," +
                    "EducationId2 INTEGER," +
                    "EducationId3 INTEGER," +
                    "EducationId4 INTEGER," +
                    "EducationId5 INTEGER," +
                    "EducationId6 INTEGER," +
                    "EducationId7 INTEGER," +
                    "EducationId8 INTEGER," +
                    "EducationId9 INTEGER," +
                    "EducationId10 INTEGER," +
                    "ProfessionId INTEGER," +
                    "ProfessionId1 INTEGER," +
                    "ProfessionId2 INTEGER," +
                    "ProfessionId3 INTEGER," +
                    "ProfessionId4 INTEGER," +
                    "ProfessionId5 INTEGER," +
                    "ProfessionId6 INTEGER," +
                    "ProfessionId7 INTEGER," +
                    "ProfessionId8 INTEGER," +
                    "ProfessionId9 INTEGER," +
                    "ProfessionId10 INTEGER," +
                    "IdentificationTypeId INTEGER," +
                    "IdentificationTypeId1 INTEGER," +
                    "IdentificationTypeId2 INTEGER," +
                    "IdentificationTypeId3 INTEGER," +
                    "IdentificationTypeId4 INTEGER," +
                    "IdentificationTypeId5 INTEGER," +
                    "IdentificationTypeId6 INTEGER," +
                    "IdentificationTypeId7 INTEGER," +
                    "IdentificationTypeId8 INTEGER," +
                    "IdentificationTypeId9 INTEGER," +
                    "IdentificationTypeId10 INTEGER," +
                    "IdentificationNumber TEXT," +
                    "IdentificationNumber1 TEXT," +
                    "IdentificationNumber2 TEXT," +
                    "IdentificationNumber3 TEXT," +
                    "IdentificationNumber4 TEXT," +
                    "IdentificationNumber5 TEXT," +
                    "IdentificationNumber6 TEXT," +
                    "IdentificationNumber7 TEXT," +
                    "IdentificationNumber8 TEXT," +
                    "IdentificationNumber9 TEXT," +
                    "IdentificationNumber10 TEXT," +
                    "FamilyId INTEGER," +
                    "FamilyId1 INTEGER," +
                    "FamilyId2 INTEGER," +
                    "FamilyId3 INTEGER," +
                    "FamilyId4 INTEGER," +
                    "FamilyId5 INTEGER," +
                    "FamilyId6 INTEGER," +
                    "FamilyId7 INTEGER," +
                    "FamilyId8 INTEGER," +
                    "FamilyId9 INTEGER," +
                    "FamilyId10 INTEGER," +
                    "LocationId INTEGER," +
                    "LocationId1 INTEGER," +
                    "LocationId2 INTEGER," +
                    "LocationId3 INTEGER," +
                    "LocationId4 INTEGER," +
                    "LocationId5 INTEGER," +
                    "LocationId6 INTEGER," +
                    "LocationId7 INTEGER," +
                    "LocationId8 INTEGER," +
                    "LocationId9 INTEGER," +
                    "LocationId10 INTEGER," +
                    "Poverty BOOLEAN," +
                    "Poverty1 BOOLEAN," +
                    "Poverty2 BOOLEAN," +
                    "Poverty3 BOOLEAN," +
                    "Poverty4 BOOLEAN," +
                    "Poverty5 BOOLEAN," +
                    "Poverty6 BOOLEAN," +
                    "Poverty7 BOOLEAN," +
                    "Poverty8 BOOLEAN," +
                    "Poverty9 BOOLEAN," +
                    "Poverty10 BOOLEAN," +
                    "RelationshipId INTEGER," +
                    "RelationshipId1 INTEGER," +
                    "RelationshipId2 INTEGER," +
                    "RelationshipId3 INTEGER," +
                    "RelationshipId4 INTEGER," +
                    "RelationshipId5 INTEGER," +
                    "RelationshipId6 INTEGER," +
                    "RelationshipId7 INTEGER," +
                    "RelationshipId8 INTEGER," +
                    "RelationshipId9 INTEGER," +
                    "RelationshipId10 INTEGER," +
                    "AuditUserId INTEGER," +
                    "AuditDate TEXT," +
                    "AuditAction TEXT," +
                    "AuditUserName TEXT," +
                    "AuditMachine TEXT," +
                    "Disability INTEGER," +
                    "DisablingDisease INTEGER," +
                    "CoverageInsurance INTEGER," +
                    "HouseType INTEGER," +
                    "ResidencePlace INTEGER" +
                    ")";

            Log.d("SQLHandler", "SQL statement for tblInsuree: " + createInsureeTable);
            sqLiteDatabase.execSQL(createInsureeTable);
            Log.d("SQLHandler", "tblInsuree table created successfully");

            // Créer les autres tables
            sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + tblConfirmationTypes + " (" +
                    "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "Code TEXT," +
                    "Name TEXT," +
                    "Name1 TEXT," +
                    "Name2 TEXT," +
                    "Name3 TEXT," +
                    "Name4 TEXT," +
                    "Name5 TEXT," +
                    "Name6 TEXT," +
                    "Name7 TEXT," +
                    "Name8 TEXT," +
                    "Name9 TEXT," +
                    "Name10 TEXT," +
                    "SortOrder INTEGER," +
                    "LastName1 TEXT," +
                    "OtherNames2 TEXT," +
                    "LastName2 TEXT," +
                    "OtherNames3 TEXT," +
                    "LastName3 TEXT," +
                    "OtherNames4 TEXT," +
                    "LastName4 TEXT," +
                    "OtherNames5 TEXT," +
                    "LastName5 TEXT," +
                    "OtherNames6 TEXT," +
                    "LastName6 TEXT," +
                    "OtherNames7 TEXT," +
                    "LastName7 TEXT," +
                    "OtherNames8 TEXT," +
                    "LastName8 TEXT," +
                    "OtherNames9 TEXT," +
                    "LastName9 TEXT," +
                    "OtherNames10 TEXT," +
                    "LastName10 TEXT," +
                    "GenderId INTEGER," +
                    "GenderId1 INTEGER," +
                    "GenderId2 INTEGER," +
                    "GenderId3 INTEGER," +
                    "GenderId4 INTEGER," +
                    "GenderId5 INTEGER," +
                    "GenderId6 INTEGER," +
                    "GenderId7 INTEGER," +
                    "GenderId8 INTEGER," +
                    "GenderId9 INTEGER," +
                    "GenderId10 INTEGER," +
                    "DateOfBirth TEXT," +
                    "DateOfBirth1 TEXT," +
                    "DateOfBirth2 TEXT," +
                    "DateOfBirth3 TEXT," +
                    "DateOfBirth4 TEXT," +
                    "DateOfBirth5 TEXT," +
                    "DateOfBirth6 TEXT," +
                    "DateOfBirth7 TEXT," +
                    "DateOfBirth8 TEXT," +
                    "DateOfBirth9 TEXT," +
                    "DateOfBirth10 TEXT," +
                    "Photo TEXT," +
                    "Photo1 TEXT," +
                    "Photo2 TEXT," +
                    "Photo3 TEXT," +
                    "Photo4 TEXT," +
                    "Photo5 TEXT," +
                    "Photo6 TEXT," +
                    "Photo7 TEXT," +
                    "Photo8 TEXT," +
                    "Photo9 TEXT," +
                    "Photo10 TEXT," +
                    "MaritalStatusId INTEGER," +
                    "MaritalStatusId1 INTEGER," +
                    "MaritalStatusId2 INTEGER," +
                    "MaritalStatusId3 INTEGER," +
                    "MaritalStatusId4 INTEGER," +
                    "MaritalStatusId5 INTEGER," +
                    "MaritalStatusId6 INTEGER," +
                    "MaritalStatusId7 INTEGER," +
                    "MaritalStatusId8 INTEGER," +
                    "MaritalStatusId9 INTEGER," +
                    "MaritalStatusId10 INTEGER," +
                    "EducationId INTEGER," +
                    "EducationId1 INTEGER," +
                    "EducationId2 INTEGER," +
                    "EducationId3 INTEGER," +
                    "EducationId4 INTEGER," +
                    "EducationId5 INTEGER," +
                    "EducationId6 INTEGER," +
                    "EducationId7 INTEGER," +
                    "EducationId8 INTEGER," +
                    "EducationId9 INTEGER," +
                    "EducationId10 INTEGER," +
                    "ProfessionId INTEGER," +
                    "ProfessionId1 INTEGER," +
                    "ProfessionId2 INTEGER," +
                    "ProfessionId3 INTEGER," +
                    "ProfessionId4 INTEGER," +
                    "ProfessionId5 INTEGER," +
                    "ProfessionId6 INTEGER," +
                    "ProfessionId7 INTEGER," +
                    "ProfessionId8 INTEGER," +
                    "ProfessionId9 INTEGER," +
                    "ProfessionId10 INTEGER," +
                    "IdentificationTypeId INTEGER," +
                    "IdentificationTypeId1 INTEGER," +
                    "IdentificationTypeId2 INTEGER," +
                    "IdentificationTypeId3 INTEGER," +
                    "IdentificationTypeId4 INTEGER," +
                    "IdentificationTypeId5 INTEGER," +
                    "IdentificationTypeId6 INTEGER," +
                    "IdentificationTypeId7 INTEGER," +
                    "IdentificationTypeId8 INTEGER," +
                    "IdentificationTypeId9 INTEGER," +
                    "IdentificationTypeId10 INTEGER," +
                    "IdentificationNumber TEXT," +
                    "IdentificationNumber1 TEXT," +
                    "IdentificationNumber2 TEXT," +
                    "IdentificationNumber3 TEXT," +
                    "IdentificationNumber4 TEXT," +
                    "IdentificationNumber5 TEXT," +
                    "IdentificationNumber6 TEXT," +
                    "IdentificationNumber7 TEXT," +
                    "IdentificationNumber8 TEXT," +
                    "IdentificationNumber9 TEXT," +
                    "IdentificationNumber10 TEXT," +
                    "FamilyId INTEGER," +
                    "FamilyId1 INTEGER," +
                    "FamilyId2 INTEGER," +
                    "FamilyId3 INTEGER," +
                    "FamilyId4 INTEGER," +
                    "FamilyId5 INTEGER," +
                    "FamilyId6 INTEGER," +
                    "FamilyId7 INTEGER," +
                    "FamilyId8 INTEGER," +
                    "FamilyId9 INTEGER," +
                    "FamilyId10 INTEGER," +
                    "LocationId INTEGER," +
                    "LocationId1 INTEGER," +
                    "LocationId2 INTEGER," +
                    "LocationId3 INTEGER," +
                    "LocationId4 INTEGER," +
                    "LocationId5 INTEGER," +
                    "LocationId6 INTEGER," +
                    "LocationId7 INTEGER," +
                    "LocationId8 INTEGER," +
                    "LocationId9 INTEGER," +
                    "LocationId10 INTEGER," +
                    "Poverty BOOLEAN," +
                    "Poverty1 BOOLEAN," +
                    "Poverty2 BOOLEAN," +
                    "Poverty3 BOOLEAN," +
                    "Poverty4 BOOLEAN," +
                    "Poverty5 BOOLEAN," +
                    "Poverty6 BOOLEAN," +
                    "Poverty7 BOOLEAN," +
                    "Poverty8 BOOLEAN," +
                    "Poverty9 BOOLEAN," +
                    "Poverty10 BOOLEAN," +
                    "RelationshipId INTEGER," +
                    "RelationshipId1 INTEGER," +
                    "RelationshipId2 INTEGER," +
                    "RelationshipId3 INTEGER," +
                    "RelationshipId4 INTEGER," +
                    "RelationshipId5 INTEGER," +
                    "RelationshipId6 INTEGER," +
                    "RelationshipId7 INTEGER," +
                    "RelationshipId8 INTEGER," +
                    "RelationshipId9 INTEGER," +
                    "RelationshipId10 INTEGER," +
                    "AuditUserId INTEGER," +
                    "AuditDate TEXT," +
                    "AuditAction TEXT," +
                    "AuditUserName TEXT," +
                    "AuditMachine TEXT," +
                    "Disability INTEGER," +
                    "DisablingDisease INTEGER," +
                    "CoverageInsurance INTEGER," +
                    "HouseType INTEGER," +
                    "ResidencePlace INTEGER" +
                    ")");
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblConfirmationTypes + "("
                            + "ConfirmationTypeCode TEXT,"
                            + "ConfirmationType TEXT NOT NULL,"
                            + "SortOrder NUMERIC NOT NULL,"
                            + "AltLanguage TEXT " + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblControlNumber + "("
                            + "Id INTEGER PRIMARY KEY AUTOINCREMENT,"
                            + "AmountCalculated INTEGER,"
                            + "AmountConfirmed INTEGER,"
                            + "ControlNumber TEXT,"
                            + "InternalIdentifier TEXT,"
                            + "PaymentType TEXT,"
                            + "SmsRequired TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblControls + "("
                            + "FieldName TEXT,"
                            + "Adjustibility TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblEducations + "("
                            + "EducationId NUMERIC,"
                            + "Education TEXT,"
                            + "SortOrder NUMERIC,"
                            + "AltLanguage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblFamilies + "(" +
                            "FamilyId INTEGER," +
                            "InsureeId NUMERIC," +
                            "InsureeChfId TEXT," +
                            "LocationId NUMERIC," +
                            "Poverty BOOLEAN," +
                            "isOffline NUMERIC," +
                            "FamilyType TEXT," +
                            "FamilyAddress TEXT," +
                            "Ethnicity TEXT," +
                            "ConfirmationNo TEXT," +
                            "ConfirmationType TEXT," +
                            "ParentId INTEGER" +
                            ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblFamilyTypes + "(" +
                            "FamilyTypeCode TEXT," +
                            "FamilyType TEXT," +
                            "SortOrder NUMERIC," +
                            "AltLanguage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblFeedbacks + "(" +
                            "ClaimId INTEGER," +
                            "ClaimUUID TEXT," +
                            "OfficerId INTEGER," +
                            "OfficerCode TEXT," +
                            "CHFID TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "HFCode TEXT," +
                            "HFName TEXT," +
                            "ClaimCode TEXT," +
                            "DateFrom TEXT," +
                            "DateTo TEXT," +
                            "IMEI TEXT," +
                            "FeedbackPromptDate TEXT," +
                            "Phone TEXT," +
                            "isDone TEXT DEFAULT 'N'" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblGender' (" +
                            "Code TEXT," +
                            "Gender TEXT," +
                            "AltLanguage TEXT," +
                            "SortOrder NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblHF' (" +
                            "HFID NUMERIC," +
                            "HFCode TEXT," +
                            "HFName TEXT," +
                            "LocationId NUMERIC," +
                            "HFLevel TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblIMISDefaultsPhone' (" +
                            "RuleName TEXT," +
                            "RuleValue BIT," +
                            "Usage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblIdentificationTypes' (" +
                            "IdentificationCode TEXT," +
                            "IdentificationTypes TEXT," +
                            "AltLanguage TEXT," +
                            "SortOrder NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblInsuree' (" +
                            "InsureeId INTEGER," +
                            "FamilyId NUMERIC," +
                            "CHFID TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "DOB TEXT," +
                            "Gender INTEGER," +
                            "Marital TEXT," +
                            "isHead NUMERIC," +
                            "IdentificationNumber TEXT," +
                            "Phone TEXT," +
                            "PhotoPath TEXT," +
                            "CardIssued BOOLEAN," +
                            "isOffline BOOLEAN," +
                            "Relationship NUMERIC," +
                            "Profession NUMERIC," +
                            "Education NUMERIC," +
                            "Email TEXT," +
                            "TypeOfId TEXT," +
                            "HFID NUMERIC," +
                            "CurrentAddress TEXT," +
                            "GeoLocation TEXT," +
                            "CurVillage NUMERIC," +
                            "Vulnerability BOOLEAN," +
                            "ProfessionalSituation TEXT," +
                            "IncomeLevel NUMERIC," +
                            "PaymentMethod TEXT," +
                            "OtherHousehold TEXT," +
                            "AccountDetails TEXT" + ")"
            );

            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblInsureePolicy' (" +
                            "InsureePolicyId INTEGER," +
                            "InsureeId INTEGER," +
                            "PolicyId NUMERIC," +
                            "EnrollmentDate DATE," +
                            "StartDate DATE," +
                            "EffectiveDate DATE," +
                            "ExpiryDate DATE," +
                            "isOffline NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblLanguages' (" +
                            "LanguageCode TEXT," +
                            "LanguageName TEXT," +
                            "SortOrder NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblLocations + " (" +
                            "LocationId NUMERIC," +
                            "LocationCode TEXT," +
                            "LocationName TEXT," +
                            "ParentLocationId NUMERIC," +
                            "LocationType TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblOfficer' (" +
                            "OfficerId NUMERIC," +
                            "Code TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "Phone TEXT," +
                            "LocationId NUMERIC," +
                            "OfficerIdSubst NUMERIC," +
                            "WorksTo DATE" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblPayer' (" +
                            "PayerId NUMERIC," +
                            "PayerName TEXT," +
                            "LocationId NUMERIC" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblPolicy' (" +
                            "PolicyId INTEGER," +
                            "PolicyUuid TEXT," +
                            "FamilyId NUMERIC," +
                            "EnrollDate DATE," +
                            "StartDate DATE," +
                            "EffectiveDate DATE," +
                            "ExpiryDate DATE," +
                            "SigningDate DATE," +
                            "PolicyStatus NUMERIC," +
                            "PolicyValue NUMERIC," +
                            "ProdId NUMERIC," +
                            "ContributionPlanId TEXT," +
                            "OfficerId NUMERIC," +
                            "isOffline NUMERIC," +
                            "Periodicity TEXT," +
                            "PaymentDay TEXT," +
                            "PolicyStage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblPremium' (" +
                            "PremiumId INTEGER," +
                            "PolicyId NUMERIC," +
                            "PayerId NUMERIC," +
                            "Amount NUMERIC," +
                            "Receipt TEXT," +
                            "PayDate DATE," +
                            "PayType TEXT," +
                            "isOffline NUMERIC," +
                            "isPhotoFee BOOLEAN" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblProduct' (" +
                            "ProdId NUMERIC," +
                            "ProductCode TEXT," +
                            "ProductName TEXT," +
                            "LocationId NUMERIC," +
                            "InsurancePeriod NUMERIC," +
                            "DateFrom DATE," +
                            "DateTo DATE," +
                            "ConversionProdId NUMERIC," +
                            "Lumpsum NUMERIC," +
                            "MemberCount NUMERIC," +
                            "PremiumAdult NUMERIC," +
                            "PremiumChild NUMERIC," +
                            "RegistrationLumpsum NUMERIC," +
                            "RegistrationFee NUMERIC," +
                            "GeneralAssemblyLumpsum NUMERIC," +
                            "GeneralAssemblyFee NUMERIC," +
                            "StartCycle1 TEXT," +
                            "StartCycle2 TEXT," +
                            "StartCycle3 TEXT," +
                            "StartCycle4 TEXT," +
                            "GracePeriodRenewal NUMERIC," +
                            "MaxInstallments NUMERIC," +
                            "WaitingPeriod NUMERIC," +
                            "Threshold NUMERIC," +
                            "RenewalDiscountPerc NUMERIC," +
                            "RenewalDiscountPeriod NUMERIC," +
                            "AdministrationPeriod NUMERIC," +
                            "EnrolmentDiscountPerc NUMERIC," +
                            "EnrolmentDiscountPeriod NUMERIC," +
                            "GracePeriod INT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblProfessions' (" +
                            "ProfessionId NUMERIC," +
                            "Profession TEXT," +
                            "SortOrder NUMERIC," +
                            "AltLanguage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblRecordedPolicies' (" +
                            "Id INTEGER PRIMARY KEY AUTOINCREMENT," +
                            "PolicyId INTEGER," +
                            "InsuranceNumber TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "ProductCode BLOB," +
                            "ProductName TEXT," +
                            "isDone TEXT DEFAULT 'N'," +
                            "PolicyValue NUMERIC," +
                            "UploadedDate TEXT," +
                            "ControlRequestDate TEXT," +
                            "Code INTEGER DEFAULT 'N'" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblRelations' (" +
                            "RelationId NUMERIC," +
                            "Relation TEXT," +
                            "SortOrder NUMERIC," +
                            "AltLanguage TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE 'tblRenewals' (" +
                            "RenewalId NUMERIC," +
                            "PolicyId INTEGER," +
                            "OfficerId INTEGER," +
                            "OfficerCode TEXT," +
                            "CHFID TEXT," +
                            "LastName TEXT," +
                            "OtherNames TEXT," +
                            "ProductCode TEXT," +
                            "ProductName TEXT," +
                            "VillageName TEXT," +
                            "RenewalPromptDate TEXT," +
                            "IMEI TEXT," +
                            "Phone TEXT," +
                            "PaymentMethod TEXT," +
                            "isDone TEXT DEFAULT 'N'," +
                            "LocationId INTEGER," +
                            "PolicyValue NUMERIC," +
                            "EnrollDate TEXT" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE VIEW uvwLocations As SELECT 'null' LocationId," +
                            " 'null' RegionId ," +
                            " 'null' RegionCode," +
                            " 'National' RegionName," +
                            " 'null' DistrictId," +
                            " 'null' DistrictName," +
                            " 'null' DistrictCode," +
                            " 'null' LocationTyPe UNION ALL SELECT LocationId," +
                            " LocationId RegionId ," +
                            " LocationCode RegionCode," +
                            " LocationName RegionName," +
                            " 'null' DistrictId," +
                            " 'null' DistrictName," +
                            " 'null' DistrictCode," +
                            "LocationTyPe FROM tbllocations" +
                            " where LocationTyPe ='R' " +
                            " UNION ALL SELECT LocationId," +
                            " ParentLocationId RegionId ," +
                            " LocationCode RegionCode," +
                            "LocationName RegionName," +
                            " LocationId DistrictId," +
                            " LocationName DistrictName," +
                            "LocationCode DistrictCode," +
                            "LocationTyPe" +
                            " FROM tbllocations " +
                            "where LocationTyPe ='D'");
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblBulkControlNumbers + "(" +
                            "Id INTEGER," +
                            "BillId INTEGER," +
                            "ProductCode TEXT," +
                            "OfficerCode TEXT," +
                            "ControlNumber TEXT," +
                            "Amount REAL," +
                            "PolicyId INTEGER" + ")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblIncomeLevel + "(" +
                            "Id INTEGER," +
                            "FirstLanguage TEXT," +
                            "SecondLanguage TEXT" +")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblContributionPlan + "(" +
                            "Id INTEGER," +
                            "Code TEXT," +
                            "Name TEXT," +
                            "ProductId INTEGER," +
                            "CalculationRules TEXT,"+
                            "Periodicity TEXT," +
                            "ValidFrom DATE," +
                            "ValidTo Date," +
                            "CpId" +")"
            );
            sqLiteDatabase.execSQL(
                    "CREATE TABLE " + tblInsureeAttachments + "(" +
                            "Id INTEGER," +
                            "Title TEXT," +
                            "Filename TEXT," +
                            "Content TEXT," +
                            "InsureeId INTEGER," +
                            "FamilyId INTEGER" + ")"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //super.onDowngrade(db, oldVersion, newVersion);
    }


    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        db.disableWriteAheadLogging();
    }

    private void openDatabase() {
        String dbPath = context.getDatabasePath(DBNAME).getPath();
        String dbOfflinePath = global.getAppDirectory() + File.separator + OFFLINEDBNAME;
        if (mDatabase != null && mDatabase.isOpen()) {
            return;
        }
        if (isPrivate)
            mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        else
            mDatabase = SQLiteDatabase.openDatabase(dbOfflinePath, null, SQLiteDatabase.OPEN_READWRITE);

    }

    public void closeDatabase() {
        if (mDatabase != null) {
            mDatabase.close();
        }
    }

    @NonNull
    public JSONArray getResult(String tableName, String[] columns, String Where, String OrderBy, String nullOverride) {
        openDatabase();
        JSONArray resultSet = new JSONArray();
        Cursor cursor = mDatabase.query(tableName, columns, Where, null, null, null, OrderBy);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int totalColumns = cursor.getColumnCount();
            JSONObject rowObject = new JSONObject();
            for (int i = 0; i < totalColumns; i++) {
                try {
                    if (cursor.getString(i) != null)
                        rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                    else
                        rowObject.put(cursor.getColumnName(i), nullOverride);
                } catch (Exception e) {
                    Log.d("Tag Name ", e.getMessage());
                }
            }

            resultSet.put(rowObject);
            cursor.moveToNext();
        }
        cursor.close();
        closeDatabase();
        return resultSet;
    }

    @NonNull
    public JSONArray getResult(String tableName, String[] columns, String where, String orderBy) {
        return getResult(tableName, columns, where, orderBy, "0");
    }

    @NonNull
    public JSONArray getResult(String Query, String[] args, String nullOverride) {
        openDatabase();
        JSONArray resultSet = new JSONArray();
        try {
            Cursor cursor = mDatabase.rawQuery(Query, args);
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                int totalColumns = cursor.getColumnCount();
                JSONObject rowObject = new JSONObject();
                for (int i = 0; i < totalColumns; i++) {
                    try {
                        if (cursor.getString(i) != null)
                            rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                        else
                            rowObject.put(cursor.getColumnName(i), nullOverride);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Log.d("Tag Name", e.getMessage());
                    }
                }
                resultSet.put(rowObject);
                cursor.moveToNext();
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        closeDatabase();
        return resultSet;
    }
    @NonNull
    public JSONArray getResult(@Language("SQL") String Query, String[] args) {
        return getResult(Query, args, "0");
    }

    public void getExportAsXML(
            @Language("SQL") String QueryF,
            @Language("SQL") String QueryI,
            @Language("SQL") String QueryPL,
            @Language("SQL") String QueryPR,
            @Language("SQL") String QueryIP,
            String OfficerCode,
            int OfficerId
    ) throws IOException {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy-HH-mm-ss");
        Calendar cal = Calendar.getInstance();
        String d = format.format(cal.getTime());

        File Dir = new File(global.getSubdirectory("Family"));

        //Here we are giving name to the XML file
        String FileName = "Enrolment_" + OfficerCode + "_" + d + ".xml";

        //Here we are creating file in that directory
        File EnrollmentXML = new File(Dir, FileName);
        //Here we are creating outputstream
        FileOutputStream fos = new FileOutputStream(EnrollmentXML, true);
        XmlSerializer serializer = Xml.newSerializer();

        serializer.setOutput(fos, "UTF-8");
        serializer.startDocument(null, Boolean.TRUE);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        serializer.startTag(null, "Enrolment");

        serializer.startTag(null, "FileInfo");

        serializer.startTag(null, "UserId");
        serializer.text("-2");
        serializer.endTag(null, "UserId");

        serializer.startTag(null, "OfficerId");
        serializer.text(String.valueOf(OfficerId));
        serializer.endTag(null, "OfficerId");

        serializer.endTag(null, "FileInfo");


        try {
            for (int i = 1; i <= 5; i++) {
                String subLabel;
                String label;
                String Query;
                if (i == 1) {
                    Query = QueryF;
                    label = "Families";
                    subLabel = "Family";
                } else if (i == 2) {
                    Query = QueryI;
                    label = "Insurees";
                    subLabel = "Insuree";
                } else if (i == 3) {
                    Query = QueryPL;
                    label = "Policies";
                    subLabel = "Policy";
                } else if (i == 4) {
                    Query = QueryIP;
                    label = "InsureePolicies";
                    subLabel = "InsureePolicy";
                } else {
                    Query = QueryPR;
                    label = "Premiums";
                    subLabel = "Premium";
                }

                serializer.startTag(null, label);
                openDatabase();
                Cursor cursor = mDatabase.rawQuery(Query, null);
                cursor.moveToFirst();

                while (!cursor.isAfterLast()) {
                    int totalColumns = cursor.getColumnCount();
                    serializer.startTag(null, subLabel);
                    for (int j = 0; j < totalColumns; j++) {

                        if (cursor.getString(j) != null) {

                            if (label.equals("Families")) {
                                if (Objects.equals(cursor.getColumnName(j), "FamilyType")) {
                                    if (cursor.getString(j).equals("0")) {
                                        serializer.startTag(null, cursor.getColumnName(j));
                                        serializer.text("");
                                        serializer.endTag(null, cursor.getColumnName(j));
                                    }

                                } else if (Objects.equals(cursor.getColumnName(j), "ConfirmationType")) {
                                    if (cursor.getString(j).equals("0")) {
                                        serializer.startTag(null, cursor.getColumnName(j));
                                        serializer.text("");
                                        serializer.endTag(null, cursor.getColumnName(j));
                                    }
                                } else if (cursor.getColumnName(j).equals("isOffline")) {
                                    String isOffline = cursor.getString(j);
                                    if (isOffline.equals("2"))
                                        isOffline = "0";
                                    serializer.startTag(null, cursor.getColumnName(j));
                                    serializer.text(isOffline);
                                    serializer.endTag(null, cursor.getColumnName(j));
                                } else {
                                    serializer.startTag(null, cursor.getColumnName(j));
                                    serializer.text(cursor.getString(j));
                                    serializer.endTag(null, cursor.getColumnName(j));
                                }
                            } else {
                                serializer.startTag(null, cursor.getColumnName(j));
                                serializer.text(cursor.getString(j));
                                serializer.endTag(null, cursor.getColumnName(j));
                            }


                        } else {
                            serializer.startTag(null, cursor.getColumnName(j));
                            serializer.text("");
                            serializer.endTag(null, cursor.getColumnName(j));
                        }
                    }
                    if (subLabel.equals("Family")) {
                        addFamilySmsTag(serializer, cursor.getString(0));
                    }
                    serializer.endTag(null, subLabel);
                    cursor.moveToNext();
                }
                serializer.endTag(null, label);
                cursor.close();
                closeDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        serializer.endTag(null, "Enrolment");
        serializer.endDocument();
        serializer.flush();
        fos.close();
    }

    private void addFamilySmsTag(@NonNull XmlSerializer serializer, String familyId) throws IOException {
        String[] args = {familyId};
        serializer.startTag(null, "FamilySMS");
        try {
            JSONObject familySMS =
                    getResult("SELECT * FROM tblFamilySMS where FamilyId = ? LIMIT 1;",
                            args).getJSONObject(0);
            serializer.startTag(null, "FamilyId");
            serializer.text(args[0]);
            serializer.endTag(null, "FamilyId");

            serializer.startTag(null, "ApprovalOfSMS");
            serializer.text(
                    String.valueOf(familySMS.getString("ApprovalOfSMS").equals("1"))
            );
            serializer.endTag(null, "ApprovalOfSMS");

            serializer.startTag(null, "LanguageOfSMS");
            serializer.text(familySMS.getString("LanguageOfSMS"));

            serializer.endTag(null, "LanguageOfSMS");
        } catch (Exception e) {
            Log.d("CreateEnrolmentXML", "Failed to create FamilySMS tag in enrolment");
            e.printStackTrace();
        }
        serializer.endTag(null, "FamilySMS");
    }

    public void insertData(String TableName, String[] Columns, String data, String PreExecute) throws JSONException {
        insertData(TableName, Columns, new JSONArray(data), PreExecute);
    }

    public void insertData(String TableName, String[] Columns, JSONArray array, String PreExecute) throws JSONException {
        String dbPath = ClientAndroidInterface.filePath;
        mDatabase = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READWRITE);
        try {
            if (array.length() == 0)
                return;

            if (!mDatabase.isOpen()) {
                openDatabase();
            }

            if (!TextUtils.isEmpty(PreExecute)) {
                mDatabase.execSQL(PreExecute);
            }


            mDatabase.beginTransaction();
            for (int i = 0; i < array.length(); i++) {
                try {
                    JSONObject object = array.getJSONObject(i);
                    ContentValues cv = new ContentValues();
                    for (String c : Columns) {
                        try {
                            cv.put(c, object.getString(c));
                        } catch (JSONException ignored) {

                        }
                    }
                    mDatabase.insert(TableName, null, cv);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            mDatabase.setTransactionSuccessful();
            mDatabase.endTransaction();
            mDatabase.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mDatabase.isOpen()) {
            closeDatabase();
        }
    }

    public void insertData(String tableName, ContentValues contentValues) {
        try {
            openDatabase();
            mDatabase.insertOrThrow(tableName, null, contentValues);
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeDatabase();
        }
    }

    public int updateData(String tableName, ContentValues contentValues, String whereClause, String[] whereArgs, boolean throwOnNoRowsUpdated) throws UserException {
        openDatabase();
        int rowsUpdated = 0;
        try {
            openDatabase();
            rowsUpdated = mDatabase.update(tableName, contentValues, whereClause, whereArgs);
            if (throwOnNoRowsUpdated && rowsUpdated <= 0) {
                throw new UserException(context.getResources().getString(R.string.ErrorUpdate));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return rowsUpdated;
    }

    public void updateData(String tableName, ContentValues contentValues, String whereClause, String[] whereArgs) throws UserException {
        updateData(tableName, contentValues, whereClause, whereArgs, true);
    }

    public void deleteData(String tableName, String whereClause, String[] whereArgs) {
        try {
            openDatabase();
            mDatabase.delete(tableName, whereClause, whereArgs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }

    public int getCount(String table, String selection, String[] selectionArgs) {
        openDatabase();
        try (Cursor c = mDatabase.query(table,
                new String[]{"COUNT(*)"},
                selection,
                selectionArgs,
                null,
                null,
                null)) {
            c.moveToFirst();
            return c.getInt(0);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } finally {
            closeDatabase();
        }
    }

    public int getAssignedCNCount(String officerCode, String productCode) {
        return getCount(tblBulkControlNumbers,
                "PolicyId IS NOT NULL AND UPPER(ProductCode) = UPPER(?) AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{productCode, officerCode});
    }

    public int getFreeCNCount(String officerCode, String productCode) {
        return getCount(tblBulkControlNumbers,
                "PolicyId IS NULL AND UPPER(ProductCode) = UPPER(?) AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{productCode, officerCode});
    }

    public int getAssignedCNCount(String officerCode) {
        return getCount(tblBulkControlNumbers,
                "PolicyId IS NOT NULL AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{officerCode});
    }

    public int getFreeCNCount(String officerCode) {
        return getCount(tblBulkControlNumbers,
                "PolicyId IS NULL AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{officerCode});
    }

    public String getProductCode(String productId) {
        openDatabase();
        String productCode = null;
        try (Cursor cursor = mDatabase.query(tblProduct,
                new String[]{"UPPER(ProductCode)"},
                "ProdId = ?",
                new String[]{productId},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                productCode = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return productCode;
    }

    public JSONArray getAvailableProducts(String officerCode) {
        SimpleDateFormat format = AppInformation.DateTimeInfo.getDefaultDateFormatter();
        String date = format.format(Calendar.getInstance().getTime());

        openDatabase();
        JSONArray result;
        try {
            String query = "SELECT DISTINCT p.ProdId, p.ProductCode, p.ProductName " +
                    "FROM tblOfficer o INNER JOIN tblLocations ld ON o.LocationId=ld.LocationId " +
                    "INNER JOIN tblLocations lr ON ld.ParentLocationId=lr.LocationId " +
                    "INNER JOIN tblProduct p ON (ld.LocationId=p.LocationId OR lr.LocationId=p.LocationId OR p.LocationId='null') " +
                    "WHERE UPPER(o.Code)=UPPER(?) and ? <= p.DateTo";

            Cursor c = mDatabase.rawQuery(query, new String[]{officerCode, date});
            result = cursorToJsonArray(c);
            c.close();
        } catch (Exception e) {
            e.printStackTrace();
            result = new JSONArray();
        } finally {
            closeDatabase();
        }
        return result;
    }

    public String getNextFreeCn(String officerCode, String productCode) {
        openDatabase();
        String result = null;
        try (Cursor c = mDatabase.query(tblBulkControlNumbers,
                new String[]{"ControlNumber"},
                "PolicyId IS NULL AND UPPER(ProductCode) = UPPER(?) AND UPPER(OfficerCode) = UPPER(?)",
                new String[]{productCode, officerCode},
                null,
                null,
                null,
                "1")) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                result = c.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return result;
    }

    public void assignCnToPolicy(int policyId, String controlNumber) {
        try {
            if (isFetchedControlNumber(controlNumber)) {
                openDatabase();
                ContentValues values = new ContentValues();
                values.put("PolicyId", policyId);
                mDatabase.update(tblBulkControlNumbers,
                        values,
                        "ControlNumber = ?",
                        new String[]{controlNumber});
            } else {
                openDatabase();
                JSONArray policyData = cursorToJsonArray(mDatabase.rawQuery(
                        "SELECT po.PolicyValue, UPPER(pr.ProductCode) as ProductCode FROM tblPolicy po INNER JOIN tblProduct pr on pr.ProdId=po.ProdId WHERE PolicyId = ?",
                        new String[]{String.valueOf(policyId)}));

                if (policyData.length() == 0) {
                    return;
                }

                ContentValues values = new ContentValues();
                values.put("OfficerCode", global.getOfficerCode());
                values.put("PolicyId", policyId);
                values.put("Amount", policyData.getJSONObject(0).getString("PolicyValue"));
                values.put("ProductCode", policyData.getJSONObject(0).getString("ProductCode"));
                values.put("ControlNumber", controlNumber);

                mDatabase.insert(tblBulkControlNumbers, null, values);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }

    public void clearCnAssignedToPolicy(int policyId) {
        openDatabase();
        ContentValues values = new ContentValues();
        values.put("PolicyId", (String) null);

        try {
            mDatabase.update(tblBulkControlNumbers,
                    values,
                    "PolicyId = ? and Id IS NOT NULL",
                    new String[]{String.valueOf(policyId)});

            mDatabase.delete(tblBulkControlNumbers,
                    "PolicyId = ? and Id IS NULL",
                    new String[]{String.valueOf(policyId)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
    }

    public boolean isFetchedControlNumber(String controlNumber) {
        return getCount(tblBulkControlNumbers,
                "ControlNumber = ? AND Id IS NOT NULL",
                new String[]{controlNumber}) > 0;
    }

    private JSONArray cursorToJsonArray(Cursor cursor) {
        JSONArray resultSet = new JSONArray();
        cursor.moveToFirst();
        try {
            while (!cursor.isAfterLast()) {
                int totalColumn = cursor.getColumnCount();
                JSONObject rowObject = new JSONObject();
                for (int i = 0; i < totalColumn; i++) {
                    if (cursor.getColumnName(i) != null) {
                        rowObject.put(cursor.getColumnName(i), cursor.getString(i));
                    }
                }
                resultSet.put(rowObject);
                cursor.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return resultSet;
    }

    public int getRegionId(int districtId) {
        openDatabase();
        int result = 0;
        try (Cursor c = mDatabase.query(tblLocations,
                new String[]{"ParentLocationId"},
                "LocationId = ?",
                new String[]{Integer.toString(districtId)},
                null,
                null,
                null,
                "1")) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                result = c.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return result;
    }

    /**
     * @return Default Language as specified by SortOrder in tblLanguages, return DEFAULT_LANGUAGE_CODE before initialization
     */
    @NonNull
    public String getDefaultLanguage() {
        openDatabase();
        String result = BuildConfig.DEFAULT_LANGUAGE_CODE;
        try (Cursor c = mDatabase.query(tblLanguages,
                new String[]{"LanguageCode"},
                null,
                null,
                null,
                null,
                "SortOrder ASC",
                "1")) {
            c.moveToFirst();
            if (!c.isAfterLast()) {
                result = c.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return result;
    }

    @NonNull
    public JSONArray getSupportedLanguages() {
        return getResult(tblLanguages, new String[]{"LanguageCode"}, null, null);
    }

    public int getProductId(String productCode) {
        openDatabase();
        String productId = null;
        try (Cursor cursor = mDatabase.query(tblProduct,
                new String[]{"ProdId"},
                "ProductCode = ?",
                new String[]{productCode},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                productId = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return Integer.parseInt(Objects.requireNonNull(productId));
    }

    public int getContributionProductId(String contributionPlanId) {
        openDatabase();
        String productId = null;
        try (Cursor cursor = mDatabase.query(tblContributionPlan,
                new String[]{"ProductId"},
                "CpId = ?",
                new String[]{contributionPlanId},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                productId = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return Integer.parseInt(Objects.requireNonNull(productId));
    }

    public int getOfficerId(String officerCode) {
        openDatabase();
        String officerId = null;
        try (Cursor cursor = mDatabase.query(tblOfficer,
                new String[]{"OfficerId"},
                "Code = ?",
                new String[]{officerCode},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                officerId = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return Integer.parseInt(Objects.requireNonNull(officerId));
    }

    public String getContributionPlanId(String contributionPlanCode) {
        openDatabase();
        String cpId = null;
        try (Cursor cursor = mDatabase.query(tblContributionPlan,
                new String[]{"CpId"},
                "Code = ?",
                new String[]{contributionPlanCode},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                cpId = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return cpId;
    }

    public String getContributionPlanCode(String contributionPlanId) {
        openDatabase();
        String cpCode = null;
        try (Cursor cursor = mDatabase.query(tblContributionPlan,
                new String[]{"Code"},
                "CpId = ?",
                new String[]{contributionPlanId},
                null,
                null,
                null,
                "1")) {
            cursor.moveToFirst();
            if (!cursor.isAfterLast()) {
                cpCode = cursor.getString(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeDatabase();
        }
        return cpCode;
    }
}

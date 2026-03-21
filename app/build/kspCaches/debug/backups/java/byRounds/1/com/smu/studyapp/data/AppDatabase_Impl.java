package com.smu.studyapp.data;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import com.smu.studyapp.data.dao.AppSessionDao;
import com.smu.studyapp.data.dao.AppSessionDao_Impl;
import com.smu.studyapp.data.dao.ParticipantDao;
import com.smu.studyapp.data.dao.ParticipantDao_Impl;
import com.smu.studyapp.data.dao.SurveyResponseDao;
import com.smu.studyapp.data.dao.SurveyResponseDao_Impl;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ParticipantDao _participantDao;

  private volatile SurveyResponseDao _surveyResponseDao;

  private volatile AppSessionDao _appSessionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(2) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `participant` (`id` INTEGER NOT NULL, `participantCode` TEXT NOT NULL, `name` TEXT NOT NULL, `age` INTEGER NOT NULL, `gender` TEXT NOT NULL, `studyGroup` TEXT NOT NULL, `enrollmentDate` INTEGER NOT NULL, `samplingWindowStart` INTEGER NOT NULL, `samplingWindowEnd` INTEGER NOT NULL, `setupComplete` INTEGER NOT NULL, `baselineSurveyComplete` INTEGER NOT NULL, `currentStudyDay` INTEGER NOT NULL, `selectedApps` TEXT NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `survey_responses` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `participantCode` TEXT NOT NULL, `surveyType` TEXT NOT NULL, `appPackage` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `studyDay` INTEGER NOT NULL, `timestamp` INTEGER NOT NULL, `responseJson` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `app_sessions` (`sessionId` TEXT NOT NULL, `participantCode` TEXT NOT NULL, `appPackage` TEXT NOT NULL, `appName` TEXT NOT NULL, `openTime` INTEGER NOT NULL, `closeTime` INTEGER NOT NULL, `studyDay` INTEGER NOT NULL, `promptShown` INTEGER NOT NULL, `promptType` TEXT NOT NULL, `satisfactionAnswered` INTEGER NOT NULL, `withinSamplingWindow` INTEGER NOT NULL, PRIMARY KEY(`sessionId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '9d4c49eb745f6d580838d517e140011b')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `participant`");
        db.execSQL("DROP TABLE IF EXISTS `survey_responses`");
        db.execSQL("DROP TABLE IF EXISTS `app_sessions`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsParticipant = new HashMap<String, TableInfo.Column>(13);
        _columnsParticipant.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("participantCode", new TableInfo.Column("participantCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("age", new TableInfo.Column("age", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("gender", new TableInfo.Column("gender", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("studyGroup", new TableInfo.Column("studyGroup", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("enrollmentDate", new TableInfo.Column("enrollmentDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("samplingWindowStart", new TableInfo.Column("samplingWindowStart", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("samplingWindowEnd", new TableInfo.Column("samplingWindowEnd", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("setupComplete", new TableInfo.Column("setupComplete", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("baselineSurveyComplete", new TableInfo.Column("baselineSurveyComplete", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("currentStudyDay", new TableInfo.Column("currentStudyDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsParticipant.put("selectedApps", new TableInfo.Column("selectedApps", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysParticipant = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesParticipant = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoParticipant = new TableInfo("participant", _columnsParticipant, _foreignKeysParticipant, _indicesParticipant);
        final TableInfo _existingParticipant = TableInfo.read(db, "participant");
        if (!_infoParticipant.equals(_existingParticipant)) {
          return new RoomOpenHelper.ValidationResult(false, "participant(com.smu.studyapp.data.entities.Participant).\n"
                  + " Expected:\n" + _infoParticipant + "\n"
                  + " Found:\n" + _existingParticipant);
        }
        final HashMap<String, TableInfo.Column> _columnsSurveyResponses = new HashMap<String, TableInfo.Column>(8);
        _columnsSurveyResponses.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyResponses.put("participantCode", new TableInfo.Column("participantCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyResponses.put("surveyType", new TableInfo.Column("surveyType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyResponses.put("appPackage", new TableInfo.Column("appPackage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyResponses.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyResponses.put("studyDay", new TableInfo.Column("studyDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyResponses.put("timestamp", new TableInfo.Column("timestamp", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyResponses.put("responseJson", new TableInfo.Column("responseJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSurveyResponses = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSurveyResponses = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSurveyResponses = new TableInfo("survey_responses", _columnsSurveyResponses, _foreignKeysSurveyResponses, _indicesSurveyResponses);
        final TableInfo _existingSurveyResponses = TableInfo.read(db, "survey_responses");
        if (!_infoSurveyResponses.equals(_existingSurveyResponses)) {
          return new RoomOpenHelper.ValidationResult(false, "survey_responses(com.smu.studyapp.data.entities.SurveyResponse).\n"
                  + " Expected:\n" + _infoSurveyResponses + "\n"
                  + " Found:\n" + _existingSurveyResponses);
        }
        final HashMap<String, TableInfo.Column> _columnsAppSessions = new HashMap<String, TableInfo.Column>(11);
        _columnsAppSessions.put("sessionId", new TableInfo.Column("sessionId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("participantCode", new TableInfo.Column("participantCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("appPackage", new TableInfo.Column("appPackage", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("appName", new TableInfo.Column("appName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("openTime", new TableInfo.Column("openTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("closeTime", new TableInfo.Column("closeTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("studyDay", new TableInfo.Column("studyDay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("promptShown", new TableInfo.Column("promptShown", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("promptType", new TableInfo.Column("promptType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("satisfactionAnswered", new TableInfo.Column("satisfactionAnswered", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsAppSessions.put("withinSamplingWindow", new TableInfo.Column("withinSamplingWindow", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysAppSessions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesAppSessions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoAppSessions = new TableInfo("app_sessions", _columnsAppSessions, _foreignKeysAppSessions, _indicesAppSessions);
        final TableInfo _existingAppSessions = TableInfo.read(db, "app_sessions");
        if (!_infoAppSessions.equals(_existingAppSessions)) {
          return new RoomOpenHelper.ValidationResult(false, "app_sessions(com.smu.studyapp.data.entities.AppSession).\n"
                  + " Expected:\n" + _infoAppSessions + "\n"
                  + " Found:\n" + _existingAppSessions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "9d4c49eb745f6d580838d517e140011b", "0d4beccdc05a3bd2db5479f5d579dc23");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "participant","survey_responses","app_sessions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `participant`");
      _db.execSQL("DELETE FROM `survey_responses`");
      _db.execSQL("DELETE FROM `app_sessions`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ParticipantDao.class, ParticipantDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SurveyResponseDao.class, SurveyResponseDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(AppSessionDao.class, AppSessionDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ParticipantDao participantDao() {
    if (_participantDao != null) {
      return _participantDao;
    } else {
      synchronized(this) {
        if(_participantDao == null) {
          _participantDao = new ParticipantDao_Impl(this);
        }
        return _participantDao;
      }
    }
  }

  @Override
  public SurveyResponseDao surveyResponseDao() {
    if (_surveyResponseDao != null) {
      return _surveyResponseDao;
    } else {
      synchronized(this) {
        if(_surveyResponseDao == null) {
          _surveyResponseDao = new SurveyResponseDao_Impl(this);
        }
        return _surveyResponseDao;
      }
    }
  }

  @Override
  public AppSessionDao appSessionDao() {
    if (_appSessionDao != null) {
      return _appSessionDao;
    } else {
      synchronized(this) {
        if(_appSessionDao == null) {
          _appSessionDao = new AppSessionDao_Impl(this);
        }
        return _appSessionDao;
      }
    }
  }
}

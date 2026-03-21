package com.smu.studyapp.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.smu.studyapp.data.entities.SurveyResponse;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SurveyResponseDao_Impl implements SurveyResponseDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SurveyResponse> __insertionAdapterOfSurveyResponse;

  public SurveyResponseDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSurveyResponse = new EntityInsertionAdapter<SurveyResponse>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `survey_responses` (`id`,`participantCode`,`surveyType`,`appPackage`,`sessionId`,`studyDay`,`timestamp`,`responseJson`) VALUES (nullif(?, 0),?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SurveyResponse entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getParticipantCode());
        statement.bindString(3, entity.getSurveyType());
        statement.bindString(4, entity.getAppPackage());
        statement.bindString(5, entity.getSessionId());
        statement.bindLong(6, entity.getStudyDay());
        statement.bindLong(7, entity.getTimestamp());
        statement.bindString(8, entity.getResponseJson());
      }
    };
  }

  @Override
  public Object insertResponse(final SurveyResponse response,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSurveyResponse.insert(response);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<SurveyResponse>> getAllResponses() {
    final String _sql = "SELECT * FROM survey_responses ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"survey_responses"}, new Callable<List<SurveyResponse>>() {
      @Override
      @NonNull
      public List<SurveyResponse> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfParticipantCode = CursorUtil.getColumnIndexOrThrow(_cursor, "participantCode");
          final int _cursorIndexOfSurveyType = CursorUtil.getColumnIndexOrThrow(_cursor, "surveyType");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfStudyDay = CursorUtil.getColumnIndexOrThrow(_cursor, "studyDay");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfResponseJson = CursorUtil.getColumnIndexOrThrow(_cursor, "responseJson");
          final List<SurveyResponse> _result = new ArrayList<SurveyResponse>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SurveyResponse _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpParticipantCode;
            _tmpParticipantCode = _cursor.getString(_cursorIndexOfParticipantCode);
            final String _tmpSurveyType;
            _tmpSurveyType = _cursor.getString(_cursorIndexOfSurveyType);
            final String _tmpAppPackage;
            _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpStudyDay;
            _tmpStudyDay = _cursor.getInt(_cursorIndexOfStudyDay);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpResponseJson;
            _tmpResponseJson = _cursor.getString(_cursorIndexOfResponseJson);
            _item = new SurveyResponse(_tmpId,_tmpParticipantCode,_tmpSurveyType,_tmpAppPackage,_tmpSessionId,_tmpStudyDay,_tmpTimestamp,_tmpResponseJson);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object getResponsesForDay(final int day,
      final Continuation<? super List<SurveyResponse>> $completion) {
    final String _sql = "SELECT * FROM survey_responses WHERE studyDay = ? ORDER BY timestamp DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, day);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SurveyResponse>>() {
      @Override
      @NonNull
      public List<SurveyResponse> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfParticipantCode = CursorUtil.getColumnIndexOrThrow(_cursor, "participantCode");
          final int _cursorIndexOfSurveyType = CursorUtil.getColumnIndexOrThrow(_cursor, "surveyType");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfStudyDay = CursorUtil.getColumnIndexOrThrow(_cursor, "studyDay");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final int _cursorIndexOfResponseJson = CursorUtil.getColumnIndexOrThrow(_cursor, "responseJson");
          final List<SurveyResponse> _result = new ArrayList<SurveyResponse>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SurveyResponse _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpParticipantCode;
            _tmpParticipantCode = _cursor.getString(_cursorIndexOfParticipantCode);
            final String _tmpSurveyType;
            _tmpSurveyType = _cursor.getString(_cursorIndexOfSurveyType);
            final String _tmpAppPackage;
            _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final int _tmpStudyDay;
            _tmpStudyDay = _cursor.getInt(_cursorIndexOfStudyDay);
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            final String _tmpResponseJson;
            _tmpResponseJson = _cursor.getString(_cursorIndexOfResponseJson);
            _item = new SurveyResponse(_tmpId,_tmpParticipantCode,_tmpSurveyType,_tmpAppPackage,_tmpSessionId,_tmpStudyDay,_tmpTimestamp,_tmpResponseJson);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getPromptCountForDay(final int day,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM survey_responses WHERE surveyType IN ('MRP','NP') AND studyDay = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, day);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getLastPromptTimestamp(final Continuation<? super Long> $completion) {
    final String _sql = "SELECT MAX(timestamp) FROM survey_responses WHERE surveyType IN ('MRP','NP')";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Long>() {
      @Override
      @Nullable
      public Long call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Long _result;
          if (_cursor.moveToFirst()) {
            final Long _tmp;
            if (_cursor.isNull(0)) {
              _tmp = null;
            } else {
              _tmp = _cursor.getLong(0);
            }
            _result = _tmp;
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

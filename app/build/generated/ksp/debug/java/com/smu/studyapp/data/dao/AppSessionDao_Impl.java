package com.smu.studyapp.data.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.smu.studyapp.data.entities.AppSession;
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
public final class AppSessionDao_Impl implements AppSessionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<AppSession> __insertionAdapterOfAppSession;

  private final EntityDeletionOrUpdateAdapter<AppSession> __updateAdapterOfAppSession;

  public AppSessionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfAppSession = new EntityInsertionAdapter<AppSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `app_sessions` (`sessionId`,`participantCode`,`appPackage`,`appName`,`openTime`,`closeTime`,`studyDay`,`promptShown`,`promptType`,`satisfactionAnswered`,`withinSamplingWindow`) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppSession entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getParticipantCode());
        statement.bindString(3, entity.getAppPackage());
        statement.bindString(4, entity.getAppName());
        statement.bindLong(5, entity.getOpenTime());
        statement.bindLong(6, entity.getCloseTime());
        statement.bindLong(7, entity.getStudyDay());
        final int _tmp = entity.getPromptShown() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindString(9, entity.getPromptType());
        final int _tmp_1 = entity.getSatisfactionAnswered() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getWithinSamplingWindow() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
      }
    };
    this.__updateAdapterOfAppSession = new EntityDeletionOrUpdateAdapter<AppSession>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `app_sessions` SET `sessionId` = ?,`participantCode` = ?,`appPackage` = ?,`appName` = ?,`openTime` = ?,`closeTime` = ?,`studyDay` = ?,`promptShown` = ?,`promptType` = ?,`satisfactionAnswered` = ?,`withinSamplingWindow` = ? WHERE `sessionId` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final AppSession entity) {
        statement.bindString(1, entity.getSessionId());
        statement.bindString(2, entity.getParticipantCode());
        statement.bindString(3, entity.getAppPackage());
        statement.bindString(4, entity.getAppName());
        statement.bindLong(5, entity.getOpenTime());
        statement.bindLong(6, entity.getCloseTime());
        statement.bindLong(7, entity.getStudyDay());
        final int _tmp = entity.getPromptShown() ? 1 : 0;
        statement.bindLong(8, _tmp);
        statement.bindString(9, entity.getPromptType());
        final int _tmp_1 = entity.getSatisfactionAnswered() ? 1 : 0;
        statement.bindLong(10, _tmp_1);
        final int _tmp_2 = entity.getWithinSamplingWindow() ? 1 : 0;
        statement.bindLong(11, _tmp_2);
        statement.bindString(12, entity.getSessionId());
      }
    };
  }

  @Override
  public Object insertSession(final AppSession session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfAppSession.insert(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateSession(final AppSession session,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfAppSession.handle(session);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object getSession(final String sessionId,
      final Continuation<? super AppSession> $completion) {
    final String _sql = "SELECT * FROM app_sessions WHERE sessionId = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, sessionId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<AppSession>() {
      @Override
      @Nullable
      public AppSession call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfParticipantCode = CursorUtil.getColumnIndexOrThrow(_cursor, "participantCode");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfOpenTime = CursorUtil.getColumnIndexOrThrow(_cursor, "openTime");
          final int _cursorIndexOfCloseTime = CursorUtil.getColumnIndexOrThrow(_cursor, "closeTime");
          final int _cursorIndexOfStudyDay = CursorUtil.getColumnIndexOrThrow(_cursor, "studyDay");
          final int _cursorIndexOfPromptShown = CursorUtil.getColumnIndexOrThrow(_cursor, "promptShown");
          final int _cursorIndexOfPromptType = CursorUtil.getColumnIndexOrThrow(_cursor, "promptType");
          final int _cursorIndexOfSatisfactionAnswered = CursorUtil.getColumnIndexOrThrow(_cursor, "satisfactionAnswered");
          final int _cursorIndexOfWithinSamplingWindow = CursorUtil.getColumnIndexOrThrow(_cursor, "withinSamplingWindow");
          final AppSession _result;
          if (_cursor.moveToFirst()) {
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpParticipantCode;
            _tmpParticipantCode = _cursor.getString(_cursorIndexOfParticipantCode);
            final String _tmpAppPackage;
            _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final long _tmpOpenTime;
            _tmpOpenTime = _cursor.getLong(_cursorIndexOfOpenTime);
            final long _tmpCloseTime;
            _tmpCloseTime = _cursor.getLong(_cursorIndexOfCloseTime);
            final int _tmpStudyDay;
            _tmpStudyDay = _cursor.getInt(_cursorIndexOfStudyDay);
            final boolean _tmpPromptShown;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfPromptShown);
            _tmpPromptShown = _tmp != 0;
            final String _tmpPromptType;
            _tmpPromptType = _cursor.getString(_cursorIndexOfPromptType);
            final boolean _tmpSatisfactionAnswered;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSatisfactionAnswered);
            _tmpSatisfactionAnswered = _tmp_1 != 0;
            final boolean _tmpWithinSamplingWindow;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfWithinSamplingWindow);
            _tmpWithinSamplingWindow = _tmp_2 != 0;
            _result = new AppSession(_tmpSessionId,_tmpParticipantCode,_tmpAppPackage,_tmpAppName,_tmpOpenTime,_tmpCloseTime,_tmpStudyDay,_tmpPromptShown,_tmpPromptType,_tmpSatisfactionAnswered,_tmpWithinSamplingWindow);
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

  @Override
  public Object getSessionsForDay(final int day,
      final Continuation<? super List<AppSession>> $completion) {
    final String _sql = "SELECT * FROM app_sessions WHERE studyDay = ? ORDER BY openTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, day);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<AppSession>>() {
      @Override
      @NonNull
      public List<AppSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfParticipantCode = CursorUtil.getColumnIndexOrThrow(_cursor, "participantCode");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfOpenTime = CursorUtil.getColumnIndexOrThrow(_cursor, "openTime");
          final int _cursorIndexOfCloseTime = CursorUtil.getColumnIndexOrThrow(_cursor, "closeTime");
          final int _cursorIndexOfStudyDay = CursorUtil.getColumnIndexOrThrow(_cursor, "studyDay");
          final int _cursorIndexOfPromptShown = CursorUtil.getColumnIndexOrThrow(_cursor, "promptShown");
          final int _cursorIndexOfPromptType = CursorUtil.getColumnIndexOrThrow(_cursor, "promptType");
          final int _cursorIndexOfSatisfactionAnswered = CursorUtil.getColumnIndexOrThrow(_cursor, "satisfactionAnswered");
          final int _cursorIndexOfWithinSamplingWindow = CursorUtil.getColumnIndexOrThrow(_cursor, "withinSamplingWindow");
          final List<AppSession> _result = new ArrayList<AppSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppSession _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpParticipantCode;
            _tmpParticipantCode = _cursor.getString(_cursorIndexOfParticipantCode);
            final String _tmpAppPackage;
            _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final long _tmpOpenTime;
            _tmpOpenTime = _cursor.getLong(_cursorIndexOfOpenTime);
            final long _tmpCloseTime;
            _tmpCloseTime = _cursor.getLong(_cursorIndexOfCloseTime);
            final int _tmpStudyDay;
            _tmpStudyDay = _cursor.getInt(_cursorIndexOfStudyDay);
            final boolean _tmpPromptShown;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfPromptShown);
            _tmpPromptShown = _tmp != 0;
            final String _tmpPromptType;
            _tmpPromptType = _cursor.getString(_cursorIndexOfPromptType);
            final boolean _tmpSatisfactionAnswered;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSatisfactionAnswered);
            _tmpSatisfactionAnswered = _tmp_1 != 0;
            final boolean _tmpWithinSamplingWindow;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfWithinSamplingWindow);
            _tmpWithinSamplingWindow = _tmp_2 != 0;
            _item = new AppSession(_tmpSessionId,_tmpParticipantCode,_tmpAppPackage,_tmpAppName,_tmpOpenTime,_tmpCloseTime,_tmpStudyDay,_tmpPromptShown,_tmpPromptType,_tmpSatisfactionAnswered,_tmpWithinSamplingWindow);
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
  public Flow<List<AppSession>> getAllSessionsFlow() {
    final String _sql = "SELECT * FROM app_sessions ORDER BY openTime DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"app_sessions"}, new Callable<List<AppSession>>() {
      @Override
      @NonNull
      public List<AppSession> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfSessionId = CursorUtil.getColumnIndexOrThrow(_cursor, "sessionId");
          final int _cursorIndexOfParticipantCode = CursorUtil.getColumnIndexOrThrow(_cursor, "participantCode");
          final int _cursorIndexOfAppPackage = CursorUtil.getColumnIndexOrThrow(_cursor, "appPackage");
          final int _cursorIndexOfAppName = CursorUtil.getColumnIndexOrThrow(_cursor, "appName");
          final int _cursorIndexOfOpenTime = CursorUtil.getColumnIndexOrThrow(_cursor, "openTime");
          final int _cursorIndexOfCloseTime = CursorUtil.getColumnIndexOrThrow(_cursor, "closeTime");
          final int _cursorIndexOfStudyDay = CursorUtil.getColumnIndexOrThrow(_cursor, "studyDay");
          final int _cursorIndexOfPromptShown = CursorUtil.getColumnIndexOrThrow(_cursor, "promptShown");
          final int _cursorIndexOfPromptType = CursorUtil.getColumnIndexOrThrow(_cursor, "promptType");
          final int _cursorIndexOfSatisfactionAnswered = CursorUtil.getColumnIndexOrThrow(_cursor, "satisfactionAnswered");
          final int _cursorIndexOfWithinSamplingWindow = CursorUtil.getColumnIndexOrThrow(_cursor, "withinSamplingWindow");
          final List<AppSession> _result = new ArrayList<AppSession>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final AppSession _item;
            final String _tmpSessionId;
            _tmpSessionId = _cursor.getString(_cursorIndexOfSessionId);
            final String _tmpParticipantCode;
            _tmpParticipantCode = _cursor.getString(_cursorIndexOfParticipantCode);
            final String _tmpAppPackage;
            _tmpAppPackage = _cursor.getString(_cursorIndexOfAppPackage);
            final String _tmpAppName;
            _tmpAppName = _cursor.getString(_cursorIndexOfAppName);
            final long _tmpOpenTime;
            _tmpOpenTime = _cursor.getLong(_cursorIndexOfOpenTime);
            final long _tmpCloseTime;
            _tmpCloseTime = _cursor.getLong(_cursorIndexOfCloseTime);
            final int _tmpStudyDay;
            _tmpStudyDay = _cursor.getInt(_cursorIndexOfStudyDay);
            final boolean _tmpPromptShown;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfPromptShown);
            _tmpPromptShown = _tmp != 0;
            final String _tmpPromptType;
            _tmpPromptType = _cursor.getString(_cursorIndexOfPromptType);
            final boolean _tmpSatisfactionAnswered;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfSatisfactionAnswered);
            _tmpSatisfactionAnswered = _tmp_1 != 0;
            final boolean _tmpWithinSamplingWindow;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfWithinSamplingWindow);
            _tmpWithinSamplingWindow = _tmp_2 != 0;
            _item = new AppSession(_tmpSessionId,_tmpParticipantCode,_tmpAppPackage,_tmpAppName,_tmpOpenTime,_tmpCloseTime,_tmpStudyDay,_tmpPromptShown,_tmpPromptType,_tmpSatisfactionAnswered,_tmpWithinSamplingWindow);
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
  public Object getPromptedSessionCountForDay(final int day,
      final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM app_sessions WHERE promptShown = 1 AND studyDay = ?";
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
  public Object getLastPromptedSessionOpenTime(final Continuation<? super Long> $completion) {
    final String _sql = "SELECT MAX(openTime) FROM app_sessions WHERE promptShown = 1";
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

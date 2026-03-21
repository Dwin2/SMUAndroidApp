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
import com.smu.studyapp.data.entities.Participant;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ParticipantDao_Impl implements ParticipantDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Participant> __insertionAdapterOfParticipant;

  private final EntityDeletionOrUpdateAdapter<Participant> __updateAdapterOfParticipant;

  public ParticipantDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfParticipant = new EntityInsertionAdapter<Participant>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `participant` (`id`,`participantCode`,`name`,`age`,`gender`,`studyGroup`,`enrollmentDate`,`samplingWindowStart`,`samplingWindowEnd`,`setupComplete`,`baselineSurveyComplete`,`currentStudyDay`,`selectedApps`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Participant entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getParticipantCode());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getAge());
        statement.bindString(5, entity.getGender());
        statement.bindString(6, entity.getStudyGroup());
        statement.bindLong(7, entity.getEnrollmentDate());
        statement.bindLong(8, entity.getSamplingWindowStart());
        statement.bindLong(9, entity.getSamplingWindowEnd());
        final int _tmp = entity.getSetupComplete() ? 1 : 0;
        statement.bindLong(10, _tmp);
        final int _tmp_1 = entity.getBaselineSurveyComplete() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getCurrentStudyDay());
        statement.bindString(13, entity.getSelectedApps());
      }
    };
    this.__updateAdapterOfParticipant = new EntityDeletionOrUpdateAdapter<Participant>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `participant` SET `id` = ?,`participantCode` = ?,`name` = ?,`age` = ?,`gender` = ?,`studyGroup` = ?,`enrollmentDate` = ?,`samplingWindowStart` = ?,`samplingWindowEnd` = ?,`setupComplete` = ?,`baselineSurveyComplete` = ?,`currentStudyDay` = ?,`selectedApps` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Participant entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getParticipantCode());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getAge());
        statement.bindString(5, entity.getGender());
        statement.bindString(6, entity.getStudyGroup());
        statement.bindLong(7, entity.getEnrollmentDate());
        statement.bindLong(8, entity.getSamplingWindowStart());
        statement.bindLong(9, entity.getSamplingWindowEnd());
        final int _tmp = entity.getSetupComplete() ? 1 : 0;
        statement.bindLong(10, _tmp);
        final int _tmp_1 = entity.getBaselineSurveyComplete() ? 1 : 0;
        statement.bindLong(11, _tmp_1);
        statement.bindLong(12, entity.getCurrentStudyDay());
        statement.bindString(13, entity.getSelectedApps());
        statement.bindLong(14, entity.getId());
      }
    };
  }

  @Override
  public Object insertParticipant(final Participant participant,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfParticipant.insert(participant);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateParticipant(final Participant participant,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfParticipant.handle(participant);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<Participant> getParticipantFlow() {
    final String _sql = "SELECT * FROM participant WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"participant"}, new Callable<Participant>() {
      @Override
      @Nullable
      public Participant call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfParticipantCode = CursorUtil.getColumnIndexOrThrow(_cursor, "participantCode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfStudyGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "studyGroup");
          final int _cursorIndexOfEnrollmentDate = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentDate");
          final int _cursorIndexOfSamplingWindowStart = CursorUtil.getColumnIndexOrThrow(_cursor, "samplingWindowStart");
          final int _cursorIndexOfSamplingWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "samplingWindowEnd");
          final int _cursorIndexOfSetupComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "setupComplete");
          final int _cursorIndexOfBaselineSurveyComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineSurveyComplete");
          final int _cursorIndexOfCurrentStudyDay = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStudyDay");
          final int _cursorIndexOfSelectedApps = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedApps");
          final Participant _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpParticipantCode;
            _tmpParticipantCode = _cursor.getString(_cursorIndexOfParticipantCode);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpGender;
            _tmpGender = _cursor.getString(_cursorIndexOfGender);
            final String _tmpStudyGroup;
            _tmpStudyGroup = _cursor.getString(_cursorIndexOfStudyGroup);
            final long _tmpEnrollmentDate;
            _tmpEnrollmentDate = _cursor.getLong(_cursorIndexOfEnrollmentDate);
            final int _tmpSamplingWindowStart;
            _tmpSamplingWindowStart = _cursor.getInt(_cursorIndexOfSamplingWindowStart);
            final int _tmpSamplingWindowEnd;
            _tmpSamplingWindowEnd = _cursor.getInt(_cursorIndexOfSamplingWindowEnd);
            final boolean _tmpSetupComplete;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSetupComplete);
            _tmpSetupComplete = _tmp != 0;
            final boolean _tmpBaselineSurveyComplete;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBaselineSurveyComplete);
            _tmpBaselineSurveyComplete = _tmp_1 != 0;
            final int _tmpCurrentStudyDay;
            _tmpCurrentStudyDay = _cursor.getInt(_cursorIndexOfCurrentStudyDay);
            final String _tmpSelectedApps;
            _tmpSelectedApps = _cursor.getString(_cursorIndexOfSelectedApps);
            _result = new Participant(_tmpId,_tmpParticipantCode,_tmpName,_tmpAge,_tmpGender,_tmpStudyGroup,_tmpEnrollmentDate,_tmpSamplingWindowStart,_tmpSamplingWindowEnd,_tmpSetupComplete,_tmpBaselineSurveyComplete,_tmpCurrentStudyDay,_tmpSelectedApps);
          } else {
            _result = null;
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
  public Object getParticipant(final Continuation<? super Participant> $completion) {
    final String _sql = "SELECT * FROM participant WHERE id = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Participant>() {
      @Override
      @Nullable
      public Participant call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfParticipantCode = CursorUtil.getColumnIndexOrThrow(_cursor, "participantCode");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAge = CursorUtil.getColumnIndexOrThrow(_cursor, "age");
          final int _cursorIndexOfGender = CursorUtil.getColumnIndexOrThrow(_cursor, "gender");
          final int _cursorIndexOfStudyGroup = CursorUtil.getColumnIndexOrThrow(_cursor, "studyGroup");
          final int _cursorIndexOfEnrollmentDate = CursorUtil.getColumnIndexOrThrow(_cursor, "enrollmentDate");
          final int _cursorIndexOfSamplingWindowStart = CursorUtil.getColumnIndexOrThrow(_cursor, "samplingWindowStart");
          final int _cursorIndexOfSamplingWindowEnd = CursorUtil.getColumnIndexOrThrow(_cursor, "samplingWindowEnd");
          final int _cursorIndexOfSetupComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "setupComplete");
          final int _cursorIndexOfBaselineSurveyComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "baselineSurveyComplete");
          final int _cursorIndexOfCurrentStudyDay = CursorUtil.getColumnIndexOrThrow(_cursor, "currentStudyDay");
          final int _cursorIndexOfSelectedApps = CursorUtil.getColumnIndexOrThrow(_cursor, "selectedApps");
          final Participant _result;
          if (_cursor.moveToFirst()) {
            final int _tmpId;
            _tmpId = _cursor.getInt(_cursorIndexOfId);
            final String _tmpParticipantCode;
            _tmpParticipantCode = _cursor.getString(_cursorIndexOfParticipantCode);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpAge;
            _tmpAge = _cursor.getInt(_cursorIndexOfAge);
            final String _tmpGender;
            _tmpGender = _cursor.getString(_cursorIndexOfGender);
            final String _tmpStudyGroup;
            _tmpStudyGroup = _cursor.getString(_cursorIndexOfStudyGroup);
            final long _tmpEnrollmentDate;
            _tmpEnrollmentDate = _cursor.getLong(_cursorIndexOfEnrollmentDate);
            final int _tmpSamplingWindowStart;
            _tmpSamplingWindowStart = _cursor.getInt(_cursorIndexOfSamplingWindowStart);
            final int _tmpSamplingWindowEnd;
            _tmpSamplingWindowEnd = _cursor.getInt(_cursorIndexOfSamplingWindowEnd);
            final boolean _tmpSetupComplete;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfSetupComplete);
            _tmpSetupComplete = _tmp != 0;
            final boolean _tmpBaselineSurveyComplete;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfBaselineSurveyComplete);
            _tmpBaselineSurveyComplete = _tmp_1 != 0;
            final int _tmpCurrentStudyDay;
            _tmpCurrentStudyDay = _cursor.getInt(_cursorIndexOfCurrentStudyDay);
            final String _tmpSelectedApps;
            _tmpSelectedApps = _cursor.getString(_cursorIndexOfSelectedApps);
            _result = new Participant(_tmpId,_tmpParticipantCode,_tmpName,_tmpAge,_tmpGender,_tmpStudyGroup,_tmpEnrollmentDate,_tmpSamplingWindowStart,_tmpSamplingWindowEnd,_tmpSetupComplete,_tmpBaselineSurveyComplete,_tmpCurrentStudyDay,_tmpSelectedApps);
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

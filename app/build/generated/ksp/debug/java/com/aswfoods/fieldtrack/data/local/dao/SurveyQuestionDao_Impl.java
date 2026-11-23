package com.aswfoods.fieldtrack.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.aswfoods.fieldtrack.data.local.entity.SurveyQuestionEntity;
import java.lang.Class;
import java.lang.Exception;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class SurveyQuestionDao_Impl implements SurveyQuestionDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SurveyQuestionEntity> __insertionAdapterOfSurveyQuestionEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearByRole;

  public SurveyQuestionDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSurveyQuestionEntity = new EntityInsertionAdapter<SurveyQuestionEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `survey_questions` (`id`,`role`,`questionCode`,`aspect`,`questionText`,`answerType`,`scoringGuide`,`weight`,`isActive`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SurveyQuestionEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getRole());
        statement.bindString(3, entity.getQuestionCode());
        statement.bindString(4, entity.getAspect());
        statement.bindString(5, entity.getQuestionText());
        statement.bindString(6, entity.getAnswerType());
        if (entity.getScoringGuide() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getScoringGuide());
        }
        statement.bindDouble(8, entity.getWeight());
        final int _tmp = entity.isActive() ? 1 : 0;
        statement.bindLong(9, _tmp);
      }
    };
    this.__preparedStmtOfClearByRole = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM survey_questions WHERE role = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<SurveyQuestionEntity> questions,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSurveyQuestionEntity.insert(questions);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearByRole(final String role, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearByRole.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, role);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfClearByRole.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getQuestionsByRole(final String role,
      final Continuation<? super List<SurveyQuestionEntity>> $completion) {
    final String _sql = "SELECT * FROM survey_questions WHERE role = ? AND isActive = 1 ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, role);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SurveyQuestionEntity>>() {
      @Override
      @NonNull
      public List<SurveyQuestionEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfRole = CursorUtil.getColumnIndexOrThrow(_cursor, "role");
          final int _cursorIndexOfQuestionCode = CursorUtil.getColumnIndexOrThrow(_cursor, "questionCode");
          final int _cursorIndexOfAspect = CursorUtil.getColumnIndexOrThrow(_cursor, "aspect");
          final int _cursorIndexOfQuestionText = CursorUtil.getColumnIndexOrThrow(_cursor, "questionText");
          final int _cursorIndexOfAnswerType = CursorUtil.getColumnIndexOrThrow(_cursor, "answerType");
          final int _cursorIndexOfScoringGuide = CursorUtil.getColumnIndexOrThrow(_cursor, "scoringGuide");
          final int _cursorIndexOfWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "weight");
          final int _cursorIndexOfIsActive = CursorUtil.getColumnIndexOrThrow(_cursor, "isActive");
          final List<SurveyQuestionEntity> _result = new ArrayList<SurveyQuestionEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SurveyQuestionEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpRole;
            _tmpRole = _cursor.getString(_cursorIndexOfRole);
            final String _tmpQuestionCode;
            _tmpQuestionCode = _cursor.getString(_cursorIndexOfQuestionCode);
            final String _tmpAspect;
            _tmpAspect = _cursor.getString(_cursorIndexOfAspect);
            final String _tmpQuestionText;
            _tmpQuestionText = _cursor.getString(_cursorIndexOfQuestionText);
            final String _tmpAnswerType;
            _tmpAnswerType = _cursor.getString(_cursorIndexOfAnswerType);
            final String _tmpScoringGuide;
            if (_cursor.isNull(_cursorIndexOfScoringGuide)) {
              _tmpScoringGuide = null;
            } else {
              _tmpScoringGuide = _cursor.getString(_cursorIndexOfScoringGuide);
            }
            final double _tmpWeight;
            _tmpWeight = _cursor.getDouble(_cursorIndexOfWeight);
            final boolean _tmpIsActive;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsActive);
            _tmpIsActive = _tmp != 0;
            _item = new SurveyQuestionEntity(_tmpId,_tmpRole,_tmpQuestionCode,_tmpAspect,_tmpQuestionText,_tmpAnswerType,_tmpScoringGuide,_tmpWeight,_tmpIsActive);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

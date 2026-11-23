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
import com.aswfoods.fieldtrack.data.local.entity.SupervisorEntity;
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
public final class SupervisorDao_Impl implements SupervisorDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<SupervisorEntity> __insertionAdapterOfSupervisorEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public SupervisorDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfSupervisorEntity = new EntityInsertionAdapter<SupervisorEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `supervisors` (`id`,`salesCode`,`nip`,`name`,`areaName`,`coverageDistributors`,`photoUrl`,`isTarget`) VALUES (?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final SupervisorEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSalesCode());
        statement.bindString(3, entity.getNip());
        statement.bindString(4, entity.getName());
        if (entity.getAreaName() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getAreaName());
        }
        if (entity.getCoverageDistributors() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getCoverageDistributors());
        }
        statement.bindString(7, entity.getPhotoUrl());
        final int _tmp = entity.isTarget() ? 1 : 0;
        statement.bindLong(8, _tmp);
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM supervisors";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<SupervisorEntity> supervisors,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfSupervisorEntity.insert(supervisors);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object getAllSupervisors(final Continuation<? super List<SupervisorEntity>> $completion) {
    final String _sql = "SELECT * FROM supervisors ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<SupervisorEntity>>() {
      @Override
      @NonNull
      public List<SupervisorEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSalesCode = CursorUtil.getColumnIndexOrThrow(_cursor, "salesCode");
          final int _cursorIndexOfNip = CursorUtil.getColumnIndexOrThrow(_cursor, "nip");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfAreaName = CursorUtil.getColumnIndexOrThrow(_cursor, "areaName");
          final int _cursorIndexOfCoverageDistributors = CursorUtil.getColumnIndexOrThrow(_cursor, "coverageDistributors");
          final int _cursorIndexOfPhotoUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "photoUrl");
          final int _cursorIndexOfIsTarget = CursorUtil.getColumnIndexOrThrow(_cursor, "isTarget");
          final List<SupervisorEntity> _result = new ArrayList<SupervisorEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final SupervisorEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSalesCode;
            _tmpSalesCode = _cursor.getString(_cursorIndexOfSalesCode);
            final String _tmpNip;
            _tmpNip = _cursor.getString(_cursorIndexOfNip);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpAreaName;
            if (_cursor.isNull(_cursorIndexOfAreaName)) {
              _tmpAreaName = null;
            } else {
              _tmpAreaName = _cursor.getString(_cursorIndexOfAreaName);
            }
            final String _tmpCoverageDistributors;
            if (_cursor.isNull(_cursorIndexOfCoverageDistributors)) {
              _tmpCoverageDistributors = null;
            } else {
              _tmpCoverageDistributors = _cursor.getString(_cursorIndexOfCoverageDistributors);
            }
            final String _tmpPhotoUrl;
            _tmpPhotoUrl = _cursor.getString(_cursorIndexOfPhotoUrl);
            final boolean _tmpIsTarget;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTarget);
            _tmpIsTarget = _tmp != 0;
            _item = new SupervisorEntity(_tmpId,_tmpSalesCode,_tmpNip,_tmpName,_tmpAreaName,_tmpCoverageDistributors,_tmpPhotoUrl,_tmpIsTarget);
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

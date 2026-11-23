package com.aswfoods.fieldtrack.data.local.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.aswfoods.fieldtrack.data.local.entity.PendingVisitEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
import java.lang.Integer;
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
public final class PendingVisitDao_Impl implements PendingVisitDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<PendingVisitEntity> __insertionAdapterOfPendingVisitEntity;

  private final SharedSQLiteStatement __preparedStmtOfResetFailedToPending;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsSynced;

  private final SharedSQLiteStatement __preparedStmtOfMarkAsFailed;

  public PendingVisitDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPendingVisitEntity = new EntityInsertionAdapter<PendingVisitEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `pending_visits` (`offlineSyncId`,`outletId`,`outletName`,`roleType`,`visitDate`,`checkinLatitude`,`checkinLongitude`,`checkinAt`,`checkoutAt`,`payloadJson`,`syncStatus`,`errorMessage`,`isIncomplete`,`needsRevisit`,`revisitReason`,`localScore`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final PendingVisitEntity entity) {
        statement.bindString(1, entity.getOfflineSyncId());
        statement.bindLong(2, entity.getOutletId());
        statement.bindString(3, entity.getOutletName());
        statement.bindString(4, entity.getRoleType());
        statement.bindString(5, entity.getVisitDate());
        if (entity.getCheckinLatitude() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getCheckinLatitude());
        }
        if (entity.getCheckinLongitude() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getCheckinLongitude());
        }
        if (entity.getCheckinAt() == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, entity.getCheckinAt());
        }
        if (entity.getCheckoutAt() == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, entity.getCheckoutAt());
        }
        statement.bindString(10, entity.getPayloadJson());
        statement.bindLong(11, entity.getSyncStatus());
        if (entity.getErrorMessage() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getErrorMessage());
        }
        final int _tmp = entity.isIncomplete() ? 1 : 0;
        statement.bindLong(13, _tmp);
        final int _tmp_1 = entity.getNeedsRevisit() ? 1 : 0;
        statement.bindLong(14, _tmp_1);
        if (entity.getRevisitReason() == null) {
          statement.bindNull(15);
        } else {
          statement.bindString(15, entity.getRevisitReason());
        }
        if (entity.getLocalScore() == null) {
          statement.bindNull(16);
        } else {
          statement.bindDouble(16, entity.getLocalScore());
        }
        statement.bindLong(17, entity.getCreatedAt());
      }
    };
    this.__preparedStmtOfResetFailedToPending = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pending_visits SET syncStatus = 0, errorMessage = NULL WHERE syncStatus = 2";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAsSynced = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pending_visits SET syncStatus = 1, errorMessage = NULL WHERE offlineSyncId = ?";
        return _query;
      }
    };
    this.__preparedStmtOfMarkAsFailed = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE pending_visits SET syncStatus = 2, errorMessage = ? WHERE offlineSyncId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertOrUpdate(final PendingVisitEntity visit,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPendingVisitEntity.insert(visit);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object resetFailedToPending(final Continuation<? super Integer> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfResetFailedToPending.acquire();
        try {
          __db.beginTransaction();
          try {
            final Integer _result = _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return _result;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfResetFailedToPending.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsSynced(final String syncId, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsSynced.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, syncId);
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
          __preparedStmtOfMarkAsSynced.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object markAsFailed(final String syncId, final String error,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkAsFailed.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, error);
        _argIndex = 2;
        _stmt.bindString(_argIndex, syncId);
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
          __preparedStmtOfMarkAsFailed.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<PendingVisitEntity>> getAllVisitsFlow() {
    final String _sql = "SELECT * FROM pending_visits ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_visits"}, new Callable<List<PendingVisitEntity>>() {
      @Override
      @NonNull
      public List<PendingVisitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfOfflineSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "offlineSyncId");
          final int _cursorIndexOfOutletId = CursorUtil.getColumnIndexOrThrow(_cursor, "outletId");
          final int _cursorIndexOfOutletName = CursorUtil.getColumnIndexOrThrow(_cursor, "outletName");
          final int _cursorIndexOfRoleType = CursorUtil.getColumnIndexOrThrow(_cursor, "roleType");
          final int _cursorIndexOfVisitDate = CursorUtil.getColumnIndexOrThrow(_cursor, "visitDate");
          final int _cursorIndexOfCheckinLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinLatitude");
          final int _cursorIndexOfCheckinLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinLongitude");
          final int _cursorIndexOfCheckinAt = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinAt");
          final int _cursorIndexOfCheckoutAt = CursorUtil.getColumnIndexOrThrow(_cursor, "checkoutAt");
          final int _cursorIndexOfPayloadJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadJson");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfIsIncomplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncomplete");
          final int _cursorIndexOfNeedsRevisit = CursorUtil.getColumnIndexOrThrow(_cursor, "needsRevisit");
          final int _cursorIndexOfRevisitReason = CursorUtil.getColumnIndexOrThrow(_cursor, "revisitReason");
          final int _cursorIndexOfLocalScore = CursorUtil.getColumnIndexOrThrow(_cursor, "localScore");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PendingVisitEntity> _result = new ArrayList<PendingVisitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PendingVisitEntity _item;
            final String _tmpOfflineSyncId;
            _tmpOfflineSyncId = _cursor.getString(_cursorIndexOfOfflineSyncId);
            final long _tmpOutletId;
            _tmpOutletId = _cursor.getLong(_cursorIndexOfOutletId);
            final String _tmpOutletName;
            _tmpOutletName = _cursor.getString(_cursorIndexOfOutletName);
            final String _tmpRoleType;
            _tmpRoleType = _cursor.getString(_cursorIndexOfRoleType);
            final String _tmpVisitDate;
            _tmpVisitDate = _cursor.getString(_cursorIndexOfVisitDate);
            final Double _tmpCheckinLatitude;
            if (_cursor.isNull(_cursorIndexOfCheckinLatitude)) {
              _tmpCheckinLatitude = null;
            } else {
              _tmpCheckinLatitude = _cursor.getDouble(_cursorIndexOfCheckinLatitude);
            }
            final Double _tmpCheckinLongitude;
            if (_cursor.isNull(_cursorIndexOfCheckinLongitude)) {
              _tmpCheckinLongitude = null;
            } else {
              _tmpCheckinLongitude = _cursor.getDouble(_cursorIndexOfCheckinLongitude);
            }
            final String _tmpCheckinAt;
            if (_cursor.isNull(_cursorIndexOfCheckinAt)) {
              _tmpCheckinAt = null;
            } else {
              _tmpCheckinAt = _cursor.getString(_cursorIndexOfCheckinAt);
            }
            final String _tmpCheckoutAt;
            if (_cursor.isNull(_cursorIndexOfCheckoutAt)) {
              _tmpCheckoutAt = null;
            } else {
              _tmpCheckoutAt = _cursor.getString(_cursorIndexOfCheckoutAt);
            }
            final String _tmpPayloadJson;
            _tmpPayloadJson = _cursor.getString(_cursorIndexOfPayloadJson);
            final int _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getInt(_cursorIndexOfSyncStatus);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final boolean _tmpIsIncomplete;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsIncomplete);
            _tmpIsIncomplete = _tmp != 0;
            final boolean _tmpNeedsRevisit;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNeedsRevisit);
            _tmpNeedsRevisit = _tmp_1 != 0;
            final String _tmpRevisitReason;
            if (_cursor.isNull(_cursorIndexOfRevisitReason)) {
              _tmpRevisitReason = null;
            } else {
              _tmpRevisitReason = _cursor.getString(_cursorIndexOfRevisitReason);
            }
            final Double _tmpLocalScore;
            if (_cursor.isNull(_cursorIndexOfLocalScore)) {
              _tmpLocalScore = null;
            } else {
              _tmpLocalScore = _cursor.getDouble(_cursorIndexOfLocalScore);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new PendingVisitEntity(_tmpOfflineSyncId,_tmpOutletId,_tmpOutletName,_tmpRoleType,_tmpVisitDate,_tmpCheckinLatitude,_tmpCheckinLongitude,_tmpCheckinAt,_tmpCheckoutAt,_tmpPayloadJson,_tmpSyncStatus,_tmpErrorMessage,_tmpIsIncomplete,_tmpNeedsRevisit,_tmpRevisitReason,_tmpLocalScore,_tmpCreatedAt);
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
  public Object getPendingVisitsToSync(
      final Continuation<? super List<PendingVisitEntity>> $completion) {
    final String _sql = "SELECT * FROM pending_visits WHERE syncStatus = 0 ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<PendingVisitEntity>>() {
      @Override
      @NonNull
      public List<PendingVisitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfOfflineSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "offlineSyncId");
          final int _cursorIndexOfOutletId = CursorUtil.getColumnIndexOrThrow(_cursor, "outletId");
          final int _cursorIndexOfOutletName = CursorUtil.getColumnIndexOrThrow(_cursor, "outletName");
          final int _cursorIndexOfRoleType = CursorUtil.getColumnIndexOrThrow(_cursor, "roleType");
          final int _cursorIndexOfVisitDate = CursorUtil.getColumnIndexOrThrow(_cursor, "visitDate");
          final int _cursorIndexOfCheckinLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinLatitude");
          final int _cursorIndexOfCheckinLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinLongitude");
          final int _cursorIndexOfCheckinAt = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinAt");
          final int _cursorIndexOfCheckoutAt = CursorUtil.getColumnIndexOrThrow(_cursor, "checkoutAt");
          final int _cursorIndexOfPayloadJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadJson");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfIsIncomplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncomplete");
          final int _cursorIndexOfNeedsRevisit = CursorUtil.getColumnIndexOrThrow(_cursor, "needsRevisit");
          final int _cursorIndexOfRevisitReason = CursorUtil.getColumnIndexOrThrow(_cursor, "revisitReason");
          final int _cursorIndexOfLocalScore = CursorUtil.getColumnIndexOrThrow(_cursor, "localScore");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PendingVisitEntity> _result = new ArrayList<PendingVisitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PendingVisitEntity _item;
            final String _tmpOfflineSyncId;
            _tmpOfflineSyncId = _cursor.getString(_cursorIndexOfOfflineSyncId);
            final long _tmpOutletId;
            _tmpOutletId = _cursor.getLong(_cursorIndexOfOutletId);
            final String _tmpOutletName;
            _tmpOutletName = _cursor.getString(_cursorIndexOfOutletName);
            final String _tmpRoleType;
            _tmpRoleType = _cursor.getString(_cursorIndexOfRoleType);
            final String _tmpVisitDate;
            _tmpVisitDate = _cursor.getString(_cursorIndexOfVisitDate);
            final Double _tmpCheckinLatitude;
            if (_cursor.isNull(_cursorIndexOfCheckinLatitude)) {
              _tmpCheckinLatitude = null;
            } else {
              _tmpCheckinLatitude = _cursor.getDouble(_cursorIndexOfCheckinLatitude);
            }
            final Double _tmpCheckinLongitude;
            if (_cursor.isNull(_cursorIndexOfCheckinLongitude)) {
              _tmpCheckinLongitude = null;
            } else {
              _tmpCheckinLongitude = _cursor.getDouble(_cursorIndexOfCheckinLongitude);
            }
            final String _tmpCheckinAt;
            if (_cursor.isNull(_cursorIndexOfCheckinAt)) {
              _tmpCheckinAt = null;
            } else {
              _tmpCheckinAt = _cursor.getString(_cursorIndexOfCheckinAt);
            }
            final String _tmpCheckoutAt;
            if (_cursor.isNull(_cursorIndexOfCheckoutAt)) {
              _tmpCheckoutAt = null;
            } else {
              _tmpCheckoutAt = _cursor.getString(_cursorIndexOfCheckoutAt);
            }
            final String _tmpPayloadJson;
            _tmpPayloadJson = _cursor.getString(_cursorIndexOfPayloadJson);
            final int _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getInt(_cursorIndexOfSyncStatus);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final boolean _tmpIsIncomplete;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsIncomplete);
            _tmpIsIncomplete = _tmp != 0;
            final boolean _tmpNeedsRevisit;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNeedsRevisit);
            _tmpNeedsRevisit = _tmp_1 != 0;
            final String _tmpRevisitReason;
            if (_cursor.isNull(_cursorIndexOfRevisitReason)) {
              _tmpRevisitReason = null;
            } else {
              _tmpRevisitReason = _cursor.getString(_cursorIndexOfRevisitReason);
            }
            final Double _tmpLocalScore;
            if (_cursor.isNull(_cursorIndexOfLocalScore)) {
              _tmpLocalScore = null;
            } else {
              _tmpLocalScore = _cursor.getDouble(_cursorIndexOfLocalScore);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new PendingVisitEntity(_tmpOfflineSyncId,_tmpOutletId,_tmpOutletName,_tmpRoleType,_tmpVisitDate,_tmpCheckinLatitude,_tmpCheckinLongitude,_tmpCheckinAt,_tmpCheckoutAt,_tmpPayloadJson,_tmpSyncStatus,_tmpErrorMessage,_tmpIsIncomplete,_tmpNeedsRevisit,_tmpRevisitReason,_tmpLocalScore,_tmpCreatedAt);
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
  public Flow<List<PendingVisitEntity>> getTodayVisitsByRoleFlow(final String roleType,
      final String visitDate) {
    final String _sql = "SELECT * FROM pending_visits WHERE roleType = ? AND visitDate = ? ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, roleType);
    _argIndex = 2;
    _statement.bindString(_argIndex, visitDate);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_visits"}, new Callable<List<PendingVisitEntity>>() {
      @Override
      @NonNull
      public List<PendingVisitEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfOfflineSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "offlineSyncId");
          final int _cursorIndexOfOutletId = CursorUtil.getColumnIndexOrThrow(_cursor, "outletId");
          final int _cursorIndexOfOutletName = CursorUtil.getColumnIndexOrThrow(_cursor, "outletName");
          final int _cursorIndexOfRoleType = CursorUtil.getColumnIndexOrThrow(_cursor, "roleType");
          final int _cursorIndexOfVisitDate = CursorUtil.getColumnIndexOrThrow(_cursor, "visitDate");
          final int _cursorIndexOfCheckinLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinLatitude");
          final int _cursorIndexOfCheckinLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinLongitude");
          final int _cursorIndexOfCheckinAt = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinAt");
          final int _cursorIndexOfCheckoutAt = CursorUtil.getColumnIndexOrThrow(_cursor, "checkoutAt");
          final int _cursorIndexOfPayloadJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadJson");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfIsIncomplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncomplete");
          final int _cursorIndexOfNeedsRevisit = CursorUtil.getColumnIndexOrThrow(_cursor, "needsRevisit");
          final int _cursorIndexOfRevisitReason = CursorUtil.getColumnIndexOrThrow(_cursor, "revisitReason");
          final int _cursorIndexOfLocalScore = CursorUtil.getColumnIndexOrThrow(_cursor, "localScore");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final List<PendingVisitEntity> _result = new ArrayList<PendingVisitEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final PendingVisitEntity _item;
            final String _tmpOfflineSyncId;
            _tmpOfflineSyncId = _cursor.getString(_cursorIndexOfOfflineSyncId);
            final long _tmpOutletId;
            _tmpOutletId = _cursor.getLong(_cursorIndexOfOutletId);
            final String _tmpOutletName;
            _tmpOutletName = _cursor.getString(_cursorIndexOfOutletName);
            final String _tmpRoleType;
            _tmpRoleType = _cursor.getString(_cursorIndexOfRoleType);
            final String _tmpVisitDate;
            _tmpVisitDate = _cursor.getString(_cursorIndexOfVisitDate);
            final Double _tmpCheckinLatitude;
            if (_cursor.isNull(_cursorIndexOfCheckinLatitude)) {
              _tmpCheckinLatitude = null;
            } else {
              _tmpCheckinLatitude = _cursor.getDouble(_cursorIndexOfCheckinLatitude);
            }
            final Double _tmpCheckinLongitude;
            if (_cursor.isNull(_cursorIndexOfCheckinLongitude)) {
              _tmpCheckinLongitude = null;
            } else {
              _tmpCheckinLongitude = _cursor.getDouble(_cursorIndexOfCheckinLongitude);
            }
            final String _tmpCheckinAt;
            if (_cursor.isNull(_cursorIndexOfCheckinAt)) {
              _tmpCheckinAt = null;
            } else {
              _tmpCheckinAt = _cursor.getString(_cursorIndexOfCheckinAt);
            }
            final String _tmpCheckoutAt;
            if (_cursor.isNull(_cursorIndexOfCheckoutAt)) {
              _tmpCheckoutAt = null;
            } else {
              _tmpCheckoutAt = _cursor.getString(_cursorIndexOfCheckoutAt);
            }
            final String _tmpPayloadJson;
            _tmpPayloadJson = _cursor.getString(_cursorIndexOfPayloadJson);
            final int _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getInt(_cursorIndexOfSyncStatus);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final boolean _tmpIsIncomplete;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsIncomplete);
            _tmpIsIncomplete = _tmp != 0;
            final boolean _tmpNeedsRevisit;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNeedsRevisit);
            _tmpNeedsRevisit = _tmp_1 != 0;
            final String _tmpRevisitReason;
            if (_cursor.isNull(_cursorIndexOfRevisitReason)) {
              _tmpRevisitReason = null;
            } else {
              _tmpRevisitReason = _cursor.getString(_cursorIndexOfRevisitReason);
            }
            final Double _tmpLocalScore;
            if (_cursor.isNull(_cursorIndexOfLocalScore)) {
              _tmpLocalScore = null;
            } else {
              _tmpLocalScore = _cursor.getDouble(_cursorIndexOfLocalScore);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _item = new PendingVisitEntity(_tmpOfflineSyncId,_tmpOutletId,_tmpOutletName,_tmpRoleType,_tmpVisitDate,_tmpCheckinLatitude,_tmpCheckinLongitude,_tmpCheckinAt,_tmpCheckoutAt,_tmpPayloadJson,_tmpSyncStatus,_tmpErrorMessage,_tmpIsIncomplete,_tmpNeedsRevisit,_tmpRevisitReason,_tmpLocalScore,_tmpCreatedAt);
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
  public Flow<Integer> countPendingVisits() {
    final String _sql = "SELECT COUNT(*) FROM pending_visits WHERE syncStatus = 0";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_visits"}, new Callable<Integer>() {
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
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Flow<Integer> countFailedVisits() {
    final String _sql = "SELECT COUNT(*) FROM pending_visits WHERE syncStatus = 2";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"pending_visits"}, new Callable<Integer>() {
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
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public Object findVisitByOutletAndRole(final long outletId, final String roleType,
      final String visitDate, final Continuation<? super PendingVisitEntity> $completion) {
    final String _sql = "SELECT * FROM pending_visits WHERE outletId = ? AND roleType = ? AND visitDate = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, outletId);
    _argIndex = 2;
    _statement.bindString(_argIndex, roleType);
    _argIndex = 3;
    _statement.bindString(_argIndex, visitDate);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<PendingVisitEntity>() {
      @Override
      @Nullable
      public PendingVisitEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfOfflineSyncId = CursorUtil.getColumnIndexOrThrow(_cursor, "offlineSyncId");
          final int _cursorIndexOfOutletId = CursorUtil.getColumnIndexOrThrow(_cursor, "outletId");
          final int _cursorIndexOfOutletName = CursorUtil.getColumnIndexOrThrow(_cursor, "outletName");
          final int _cursorIndexOfRoleType = CursorUtil.getColumnIndexOrThrow(_cursor, "roleType");
          final int _cursorIndexOfVisitDate = CursorUtil.getColumnIndexOrThrow(_cursor, "visitDate");
          final int _cursorIndexOfCheckinLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinLatitude");
          final int _cursorIndexOfCheckinLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinLongitude");
          final int _cursorIndexOfCheckinAt = CursorUtil.getColumnIndexOrThrow(_cursor, "checkinAt");
          final int _cursorIndexOfCheckoutAt = CursorUtil.getColumnIndexOrThrow(_cursor, "checkoutAt");
          final int _cursorIndexOfPayloadJson = CursorUtil.getColumnIndexOrThrow(_cursor, "payloadJson");
          final int _cursorIndexOfSyncStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "syncStatus");
          final int _cursorIndexOfErrorMessage = CursorUtil.getColumnIndexOrThrow(_cursor, "errorMessage");
          final int _cursorIndexOfIsIncomplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isIncomplete");
          final int _cursorIndexOfNeedsRevisit = CursorUtil.getColumnIndexOrThrow(_cursor, "needsRevisit");
          final int _cursorIndexOfRevisitReason = CursorUtil.getColumnIndexOrThrow(_cursor, "revisitReason");
          final int _cursorIndexOfLocalScore = CursorUtil.getColumnIndexOrThrow(_cursor, "localScore");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final PendingVisitEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpOfflineSyncId;
            _tmpOfflineSyncId = _cursor.getString(_cursorIndexOfOfflineSyncId);
            final long _tmpOutletId;
            _tmpOutletId = _cursor.getLong(_cursorIndexOfOutletId);
            final String _tmpOutletName;
            _tmpOutletName = _cursor.getString(_cursorIndexOfOutletName);
            final String _tmpRoleType;
            _tmpRoleType = _cursor.getString(_cursorIndexOfRoleType);
            final String _tmpVisitDate;
            _tmpVisitDate = _cursor.getString(_cursorIndexOfVisitDate);
            final Double _tmpCheckinLatitude;
            if (_cursor.isNull(_cursorIndexOfCheckinLatitude)) {
              _tmpCheckinLatitude = null;
            } else {
              _tmpCheckinLatitude = _cursor.getDouble(_cursorIndexOfCheckinLatitude);
            }
            final Double _tmpCheckinLongitude;
            if (_cursor.isNull(_cursorIndexOfCheckinLongitude)) {
              _tmpCheckinLongitude = null;
            } else {
              _tmpCheckinLongitude = _cursor.getDouble(_cursorIndexOfCheckinLongitude);
            }
            final String _tmpCheckinAt;
            if (_cursor.isNull(_cursorIndexOfCheckinAt)) {
              _tmpCheckinAt = null;
            } else {
              _tmpCheckinAt = _cursor.getString(_cursorIndexOfCheckinAt);
            }
            final String _tmpCheckoutAt;
            if (_cursor.isNull(_cursorIndexOfCheckoutAt)) {
              _tmpCheckoutAt = null;
            } else {
              _tmpCheckoutAt = _cursor.getString(_cursorIndexOfCheckoutAt);
            }
            final String _tmpPayloadJson;
            _tmpPayloadJson = _cursor.getString(_cursorIndexOfPayloadJson);
            final int _tmpSyncStatus;
            _tmpSyncStatus = _cursor.getInt(_cursorIndexOfSyncStatus);
            final String _tmpErrorMessage;
            if (_cursor.isNull(_cursorIndexOfErrorMessage)) {
              _tmpErrorMessage = null;
            } else {
              _tmpErrorMessage = _cursor.getString(_cursorIndexOfErrorMessage);
            }
            final boolean _tmpIsIncomplete;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsIncomplete);
            _tmpIsIncomplete = _tmp != 0;
            final boolean _tmpNeedsRevisit;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfNeedsRevisit);
            _tmpNeedsRevisit = _tmp_1 != 0;
            final String _tmpRevisitReason;
            if (_cursor.isNull(_cursorIndexOfRevisitReason)) {
              _tmpRevisitReason = null;
            } else {
              _tmpRevisitReason = _cursor.getString(_cursorIndexOfRevisitReason);
            }
            final Double _tmpLocalScore;
            if (_cursor.isNull(_cursorIndexOfLocalScore)) {
              _tmpLocalScore = null;
            } else {
              _tmpLocalScore = _cursor.getDouble(_cursorIndexOfLocalScore);
            }
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            _result = new PendingVisitEntity(_tmpOfflineSyncId,_tmpOutletId,_tmpOutletName,_tmpRoleType,_tmpVisitDate,_tmpCheckinLatitude,_tmpCheckinLongitude,_tmpCheckinAt,_tmpCheckoutAt,_tmpPayloadJson,_tmpSyncStatus,_tmpErrorMessage,_tmpIsIncomplete,_tmpNeedsRevisit,_tmpRevisitReason,_tmpLocalScore,_tmpCreatedAt);
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

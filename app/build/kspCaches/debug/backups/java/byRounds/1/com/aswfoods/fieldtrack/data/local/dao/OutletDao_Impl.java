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
import com.aswfoods.fieldtrack.data.local.entity.OutletEntity;
import java.lang.Class;
import java.lang.Double;
import java.lang.Exception;
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
public final class OutletDao_Impl implements OutletDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<OutletEntity> __insertionAdapterOfOutletEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateOutletCoordinates;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public OutletDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfOutletEntity = new EntityInsertionAdapter<OutletEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `outlets` (`id`,`branchId`,`kodeToko`,`namaToko`,`alamat`,`latitude`,`longitude`,`classOutlet`,`priceCode`,`isSewaDisplay`,`ownerName`,`contactPhone`,`stopOrder`,`outletType`,`isVisitedByMe`,`isVisitedLeader`,`isVisitedMember`,`isSurveyComplete`,`finalCombinedScore`,`needsRevisit`,`revisitReason`,`partnerName`,`isCoordinateCalibrated`,`isPareto`,`routePlanId`,`hasSurveyedBefore`,`lastSurveyDate`,`lastSurveyScore`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final OutletEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBranchId());
        statement.bindString(3, entity.getKodeToko());
        statement.bindString(4, entity.getNamaToko());
        statement.bindString(5, entity.getAlamat());
        if (entity.getLatitude() == null) {
          statement.bindNull(6);
        } else {
          statement.bindDouble(6, entity.getLatitude());
        }
        if (entity.getLongitude() == null) {
          statement.bindNull(7);
        } else {
          statement.bindDouble(7, entity.getLongitude());
        }
        statement.bindString(8, entity.getClassOutlet());
        statement.bindString(9, entity.getPriceCode());
        final int _tmp = entity.isSewaDisplay() ? 1 : 0;
        statement.bindLong(10, _tmp);
        if (entity.getOwnerName() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getOwnerName());
        }
        if (entity.getContactPhone() == null) {
          statement.bindNull(12);
        } else {
          statement.bindString(12, entity.getContactPhone());
        }
        statement.bindLong(13, entity.getStopOrder());
        statement.bindString(14, entity.getOutletType());
        final int _tmp_1 = entity.isVisitedByMe() ? 1 : 0;
        statement.bindLong(15, _tmp_1);
        final int _tmp_2 = entity.isVisitedLeader() ? 1 : 0;
        statement.bindLong(16, _tmp_2);
        final int _tmp_3 = entity.isVisitedMember() ? 1 : 0;
        statement.bindLong(17, _tmp_3);
        final int _tmp_4 = entity.isSurveyComplete() ? 1 : 0;
        statement.bindLong(18, _tmp_4);
        if (entity.getFinalCombinedScore() == null) {
          statement.bindNull(19);
        } else {
          statement.bindDouble(19, entity.getFinalCombinedScore());
        }
        final int _tmp_5 = entity.getNeedsRevisit() ? 1 : 0;
        statement.bindLong(20, _tmp_5);
        if (entity.getRevisitReason() == null) {
          statement.bindNull(21);
        } else {
          statement.bindString(21, entity.getRevisitReason());
        }
        if (entity.getPartnerName() == null) {
          statement.bindNull(22);
        } else {
          statement.bindString(22, entity.getPartnerName());
        }
        final int _tmp_6 = entity.isCoordinateCalibrated() ? 1 : 0;
        statement.bindLong(23, _tmp_6);
        final int _tmp_7 = entity.isPareto() ? 1 : 0;
        statement.bindLong(24, _tmp_7);
        if (entity.getRoutePlanId() == null) {
          statement.bindNull(25);
        } else {
          statement.bindLong(25, entity.getRoutePlanId());
        }
        final int _tmp_8 = entity.getHasSurveyedBefore() ? 1 : 0;
        statement.bindLong(26, _tmp_8);
        if (entity.getLastSurveyDate() == null) {
          statement.bindNull(27);
        } else {
          statement.bindString(27, entity.getLastSurveyDate());
        }
        if (entity.getLastSurveyScore() == null) {
          statement.bindNull(28);
        } else {
          statement.bindDouble(28, entity.getLastSurveyScore());
        }
      }
    };
    this.__preparedStmtOfUpdateOutletCoordinates = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE outlets SET latitude = ?, longitude = ?, isCoordinateCalibrated = 1 WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM outlets";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<OutletEntity> outlets,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfOutletEntity.insert(outlets);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateOutletCoordinates(final long outletId, final double latitude,
      final double longitude, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateOutletCoordinates.acquire();
        int _argIndex = 1;
        _stmt.bindDouble(_argIndex, latitude);
        _argIndex = 2;
        _stmt.bindDouble(_argIndex, longitude);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, outletId);
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
          __preparedStmtOfUpdateOutletCoordinates.release(_stmt);
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
  public Flow<List<OutletEntity>> getAllOutlets() {
    final String _sql = "SELECT * FROM outlets ORDER BY stopOrder ASC, namaToko ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"outlets"}, new Callable<List<OutletEntity>>() {
      @Override
      @NonNull
      public List<OutletEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBranchId = CursorUtil.getColumnIndexOrThrow(_cursor, "branchId");
          final int _cursorIndexOfKodeToko = CursorUtil.getColumnIndexOrThrow(_cursor, "kodeToko");
          final int _cursorIndexOfNamaToko = CursorUtil.getColumnIndexOrThrow(_cursor, "namaToko");
          final int _cursorIndexOfAlamat = CursorUtil.getColumnIndexOrThrow(_cursor, "alamat");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfClassOutlet = CursorUtil.getColumnIndexOrThrow(_cursor, "classOutlet");
          final int _cursorIndexOfPriceCode = CursorUtil.getColumnIndexOrThrow(_cursor, "priceCode");
          final int _cursorIndexOfIsSewaDisplay = CursorUtil.getColumnIndexOrThrow(_cursor, "isSewaDisplay");
          final int _cursorIndexOfOwnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerName");
          final int _cursorIndexOfContactPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "contactPhone");
          final int _cursorIndexOfStopOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "stopOrder");
          final int _cursorIndexOfOutletType = CursorUtil.getColumnIndexOrThrow(_cursor, "outletType");
          final int _cursorIndexOfIsVisitedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedByMe");
          final int _cursorIndexOfIsVisitedLeader = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedLeader");
          final int _cursorIndexOfIsVisitedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedMember");
          final int _cursorIndexOfIsSurveyComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isSurveyComplete");
          final int _cursorIndexOfFinalCombinedScore = CursorUtil.getColumnIndexOrThrow(_cursor, "finalCombinedScore");
          final int _cursorIndexOfNeedsRevisit = CursorUtil.getColumnIndexOrThrow(_cursor, "needsRevisit");
          final int _cursorIndexOfRevisitReason = CursorUtil.getColumnIndexOrThrow(_cursor, "revisitReason");
          final int _cursorIndexOfPartnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "partnerName");
          final int _cursorIndexOfIsCoordinateCalibrated = CursorUtil.getColumnIndexOrThrow(_cursor, "isCoordinateCalibrated");
          final int _cursorIndexOfIsPareto = CursorUtil.getColumnIndexOrThrow(_cursor, "isPareto");
          final int _cursorIndexOfRoutePlanId = CursorUtil.getColumnIndexOrThrow(_cursor, "routePlanId");
          final int _cursorIndexOfHasSurveyedBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "hasSurveyedBefore");
          final int _cursorIndexOfLastSurveyDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSurveyDate");
          final int _cursorIndexOfLastSurveyScore = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSurveyScore");
          final List<OutletEntity> _result = new ArrayList<OutletEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OutletEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBranchId;
            _tmpBranchId = _cursor.getLong(_cursorIndexOfBranchId);
            final String _tmpKodeToko;
            _tmpKodeToko = _cursor.getString(_cursorIndexOfKodeToko);
            final String _tmpNamaToko;
            _tmpNamaToko = _cursor.getString(_cursorIndexOfNamaToko);
            final String _tmpAlamat;
            _tmpAlamat = _cursor.getString(_cursorIndexOfAlamat);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final String _tmpClassOutlet;
            _tmpClassOutlet = _cursor.getString(_cursorIndexOfClassOutlet);
            final String _tmpPriceCode;
            _tmpPriceCode = _cursor.getString(_cursorIndexOfPriceCode);
            final boolean _tmpIsSewaDisplay;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSewaDisplay);
            _tmpIsSewaDisplay = _tmp != 0;
            final String _tmpOwnerName;
            if (_cursor.isNull(_cursorIndexOfOwnerName)) {
              _tmpOwnerName = null;
            } else {
              _tmpOwnerName = _cursor.getString(_cursorIndexOfOwnerName);
            }
            final String _tmpContactPhone;
            if (_cursor.isNull(_cursorIndexOfContactPhone)) {
              _tmpContactPhone = null;
            } else {
              _tmpContactPhone = _cursor.getString(_cursorIndexOfContactPhone);
            }
            final int _tmpStopOrder;
            _tmpStopOrder = _cursor.getInt(_cursorIndexOfStopOrder);
            final String _tmpOutletType;
            _tmpOutletType = _cursor.getString(_cursorIndexOfOutletType);
            final boolean _tmpIsVisitedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsVisitedByMe);
            _tmpIsVisitedByMe = _tmp_1 != 0;
            final boolean _tmpIsVisitedLeader;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsVisitedLeader);
            _tmpIsVisitedLeader = _tmp_2 != 0;
            final boolean _tmpIsVisitedMember;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsVisitedMember);
            _tmpIsVisitedMember = _tmp_3 != 0;
            final boolean _tmpIsSurveyComplete;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsSurveyComplete);
            _tmpIsSurveyComplete = _tmp_4 != 0;
            final Double _tmpFinalCombinedScore;
            if (_cursor.isNull(_cursorIndexOfFinalCombinedScore)) {
              _tmpFinalCombinedScore = null;
            } else {
              _tmpFinalCombinedScore = _cursor.getDouble(_cursorIndexOfFinalCombinedScore);
            }
            final boolean _tmpNeedsRevisit;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNeedsRevisit);
            _tmpNeedsRevisit = _tmp_5 != 0;
            final String _tmpRevisitReason;
            if (_cursor.isNull(_cursorIndexOfRevisitReason)) {
              _tmpRevisitReason = null;
            } else {
              _tmpRevisitReason = _cursor.getString(_cursorIndexOfRevisitReason);
            }
            final String _tmpPartnerName;
            if (_cursor.isNull(_cursorIndexOfPartnerName)) {
              _tmpPartnerName = null;
            } else {
              _tmpPartnerName = _cursor.getString(_cursorIndexOfPartnerName);
            }
            final boolean _tmpIsCoordinateCalibrated;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCoordinateCalibrated);
            _tmpIsCoordinateCalibrated = _tmp_6 != 0;
            final boolean _tmpIsPareto;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsPareto);
            _tmpIsPareto = _tmp_7 != 0;
            final Long _tmpRoutePlanId;
            if (_cursor.isNull(_cursorIndexOfRoutePlanId)) {
              _tmpRoutePlanId = null;
            } else {
              _tmpRoutePlanId = _cursor.getLong(_cursorIndexOfRoutePlanId);
            }
            final boolean _tmpHasSurveyedBefore;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfHasSurveyedBefore);
            _tmpHasSurveyedBefore = _tmp_8 != 0;
            final String _tmpLastSurveyDate;
            if (_cursor.isNull(_cursorIndexOfLastSurveyDate)) {
              _tmpLastSurveyDate = null;
            } else {
              _tmpLastSurveyDate = _cursor.getString(_cursorIndexOfLastSurveyDate);
            }
            final Double _tmpLastSurveyScore;
            if (_cursor.isNull(_cursorIndexOfLastSurveyScore)) {
              _tmpLastSurveyScore = null;
            } else {
              _tmpLastSurveyScore = _cursor.getDouble(_cursorIndexOfLastSurveyScore);
            }
            _item = new OutletEntity(_tmpId,_tmpBranchId,_tmpKodeToko,_tmpNamaToko,_tmpAlamat,_tmpLatitude,_tmpLongitude,_tmpClassOutlet,_tmpPriceCode,_tmpIsSewaDisplay,_tmpOwnerName,_tmpContactPhone,_tmpStopOrder,_tmpOutletType,_tmpIsVisitedByMe,_tmpIsVisitedLeader,_tmpIsVisitedMember,_tmpIsSurveyComplete,_tmpFinalCombinedScore,_tmpNeedsRevisit,_tmpRevisitReason,_tmpPartnerName,_tmpIsCoordinateCalibrated,_tmpIsPareto,_tmpRoutePlanId,_tmpHasSurveyedBefore,_tmpLastSurveyDate,_tmpLastSurveyScore);
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
  public Object getOutletById(final long outletId,
      final Continuation<? super OutletEntity> $completion) {
    final String _sql = "SELECT * FROM outlets WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, outletId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<OutletEntity>() {
      @Override
      @Nullable
      public OutletEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBranchId = CursorUtil.getColumnIndexOrThrow(_cursor, "branchId");
          final int _cursorIndexOfKodeToko = CursorUtil.getColumnIndexOrThrow(_cursor, "kodeToko");
          final int _cursorIndexOfNamaToko = CursorUtil.getColumnIndexOrThrow(_cursor, "namaToko");
          final int _cursorIndexOfAlamat = CursorUtil.getColumnIndexOrThrow(_cursor, "alamat");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfClassOutlet = CursorUtil.getColumnIndexOrThrow(_cursor, "classOutlet");
          final int _cursorIndexOfPriceCode = CursorUtil.getColumnIndexOrThrow(_cursor, "priceCode");
          final int _cursorIndexOfIsSewaDisplay = CursorUtil.getColumnIndexOrThrow(_cursor, "isSewaDisplay");
          final int _cursorIndexOfOwnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerName");
          final int _cursorIndexOfContactPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "contactPhone");
          final int _cursorIndexOfStopOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "stopOrder");
          final int _cursorIndexOfOutletType = CursorUtil.getColumnIndexOrThrow(_cursor, "outletType");
          final int _cursorIndexOfIsVisitedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedByMe");
          final int _cursorIndexOfIsVisitedLeader = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedLeader");
          final int _cursorIndexOfIsVisitedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedMember");
          final int _cursorIndexOfIsSurveyComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isSurveyComplete");
          final int _cursorIndexOfFinalCombinedScore = CursorUtil.getColumnIndexOrThrow(_cursor, "finalCombinedScore");
          final int _cursorIndexOfNeedsRevisit = CursorUtil.getColumnIndexOrThrow(_cursor, "needsRevisit");
          final int _cursorIndexOfRevisitReason = CursorUtil.getColumnIndexOrThrow(_cursor, "revisitReason");
          final int _cursorIndexOfPartnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "partnerName");
          final int _cursorIndexOfIsCoordinateCalibrated = CursorUtil.getColumnIndexOrThrow(_cursor, "isCoordinateCalibrated");
          final int _cursorIndexOfIsPareto = CursorUtil.getColumnIndexOrThrow(_cursor, "isPareto");
          final int _cursorIndexOfRoutePlanId = CursorUtil.getColumnIndexOrThrow(_cursor, "routePlanId");
          final int _cursorIndexOfHasSurveyedBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "hasSurveyedBefore");
          final int _cursorIndexOfLastSurveyDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSurveyDate");
          final int _cursorIndexOfLastSurveyScore = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSurveyScore");
          final OutletEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBranchId;
            _tmpBranchId = _cursor.getLong(_cursorIndexOfBranchId);
            final String _tmpKodeToko;
            _tmpKodeToko = _cursor.getString(_cursorIndexOfKodeToko);
            final String _tmpNamaToko;
            _tmpNamaToko = _cursor.getString(_cursorIndexOfNamaToko);
            final String _tmpAlamat;
            _tmpAlamat = _cursor.getString(_cursorIndexOfAlamat);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final String _tmpClassOutlet;
            _tmpClassOutlet = _cursor.getString(_cursorIndexOfClassOutlet);
            final String _tmpPriceCode;
            _tmpPriceCode = _cursor.getString(_cursorIndexOfPriceCode);
            final boolean _tmpIsSewaDisplay;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSewaDisplay);
            _tmpIsSewaDisplay = _tmp != 0;
            final String _tmpOwnerName;
            if (_cursor.isNull(_cursorIndexOfOwnerName)) {
              _tmpOwnerName = null;
            } else {
              _tmpOwnerName = _cursor.getString(_cursorIndexOfOwnerName);
            }
            final String _tmpContactPhone;
            if (_cursor.isNull(_cursorIndexOfContactPhone)) {
              _tmpContactPhone = null;
            } else {
              _tmpContactPhone = _cursor.getString(_cursorIndexOfContactPhone);
            }
            final int _tmpStopOrder;
            _tmpStopOrder = _cursor.getInt(_cursorIndexOfStopOrder);
            final String _tmpOutletType;
            _tmpOutletType = _cursor.getString(_cursorIndexOfOutletType);
            final boolean _tmpIsVisitedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsVisitedByMe);
            _tmpIsVisitedByMe = _tmp_1 != 0;
            final boolean _tmpIsVisitedLeader;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsVisitedLeader);
            _tmpIsVisitedLeader = _tmp_2 != 0;
            final boolean _tmpIsVisitedMember;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsVisitedMember);
            _tmpIsVisitedMember = _tmp_3 != 0;
            final boolean _tmpIsSurveyComplete;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsSurveyComplete);
            _tmpIsSurveyComplete = _tmp_4 != 0;
            final Double _tmpFinalCombinedScore;
            if (_cursor.isNull(_cursorIndexOfFinalCombinedScore)) {
              _tmpFinalCombinedScore = null;
            } else {
              _tmpFinalCombinedScore = _cursor.getDouble(_cursorIndexOfFinalCombinedScore);
            }
            final boolean _tmpNeedsRevisit;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNeedsRevisit);
            _tmpNeedsRevisit = _tmp_5 != 0;
            final String _tmpRevisitReason;
            if (_cursor.isNull(_cursorIndexOfRevisitReason)) {
              _tmpRevisitReason = null;
            } else {
              _tmpRevisitReason = _cursor.getString(_cursorIndexOfRevisitReason);
            }
            final String _tmpPartnerName;
            if (_cursor.isNull(_cursorIndexOfPartnerName)) {
              _tmpPartnerName = null;
            } else {
              _tmpPartnerName = _cursor.getString(_cursorIndexOfPartnerName);
            }
            final boolean _tmpIsCoordinateCalibrated;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCoordinateCalibrated);
            _tmpIsCoordinateCalibrated = _tmp_6 != 0;
            final boolean _tmpIsPareto;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsPareto);
            _tmpIsPareto = _tmp_7 != 0;
            final Long _tmpRoutePlanId;
            if (_cursor.isNull(_cursorIndexOfRoutePlanId)) {
              _tmpRoutePlanId = null;
            } else {
              _tmpRoutePlanId = _cursor.getLong(_cursorIndexOfRoutePlanId);
            }
            final boolean _tmpHasSurveyedBefore;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfHasSurveyedBefore);
            _tmpHasSurveyedBefore = _tmp_8 != 0;
            final String _tmpLastSurveyDate;
            if (_cursor.isNull(_cursorIndexOfLastSurveyDate)) {
              _tmpLastSurveyDate = null;
            } else {
              _tmpLastSurveyDate = _cursor.getString(_cursorIndexOfLastSurveyDate);
            }
            final Double _tmpLastSurveyScore;
            if (_cursor.isNull(_cursorIndexOfLastSurveyScore)) {
              _tmpLastSurveyScore = null;
            } else {
              _tmpLastSurveyScore = _cursor.getDouble(_cursorIndexOfLastSurveyScore);
            }
            _result = new OutletEntity(_tmpId,_tmpBranchId,_tmpKodeToko,_tmpNamaToko,_tmpAlamat,_tmpLatitude,_tmpLongitude,_tmpClassOutlet,_tmpPriceCode,_tmpIsSewaDisplay,_tmpOwnerName,_tmpContactPhone,_tmpStopOrder,_tmpOutletType,_tmpIsVisitedByMe,_tmpIsVisitedLeader,_tmpIsVisitedMember,_tmpIsSurveyComplete,_tmpFinalCombinedScore,_tmpNeedsRevisit,_tmpRevisitReason,_tmpPartnerName,_tmpIsCoordinateCalibrated,_tmpIsPareto,_tmpRoutePlanId,_tmpHasSurveyedBefore,_tmpLastSurveyDate,_tmpLastSurveyScore);
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
  public Object getCalibratedOutlets(final Continuation<? super List<OutletEntity>> $completion) {
    final String _sql = "SELECT * FROM outlets WHERE isCoordinateCalibrated = 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<OutletEntity>>() {
      @Override
      @NonNull
      public List<OutletEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBranchId = CursorUtil.getColumnIndexOrThrow(_cursor, "branchId");
          final int _cursorIndexOfKodeToko = CursorUtil.getColumnIndexOrThrow(_cursor, "kodeToko");
          final int _cursorIndexOfNamaToko = CursorUtil.getColumnIndexOrThrow(_cursor, "namaToko");
          final int _cursorIndexOfAlamat = CursorUtil.getColumnIndexOrThrow(_cursor, "alamat");
          final int _cursorIndexOfLatitude = CursorUtil.getColumnIndexOrThrow(_cursor, "latitude");
          final int _cursorIndexOfLongitude = CursorUtil.getColumnIndexOrThrow(_cursor, "longitude");
          final int _cursorIndexOfClassOutlet = CursorUtil.getColumnIndexOrThrow(_cursor, "classOutlet");
          final int _cursorIndexOfPriceCode = CursorUtil.getColumnIndexOrThrow(_cursor, "priceCode");
          final int _cursorIndexOfIsSewaDisplay = CursorUtil.getColumnIndexOrThrow(_cursor, "isSewaDisplay");
          final int _cursorIndexOfOwnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "ownerName");
          final int _cursorIndexOfContactPhone = CursorUtil.getColumnIndexOrThrow(_cursor, "contactPhone");
          final int _cursorIndexOfStopOrder = CursorUtil.getColumnIndexOrThrow(_cursor, "stopOrder");
          final int _cursorIndexOfOutletType = CursorUtil.getColumnIndexOrThrow(_cursor, "outletType");
          final int _cursorIndexOfIsVisitedByMe = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedByMe");
          final int _cursorIndexOfIsVisitedLeader = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedLeader");
          final int _cursorIndexOfIsVisitedMember = CursorUtil.getColumnIndexOrThrow(_cursor, "isVisitedMember");
          final int _cursorIndexOfIsSurveyComplete = CursorUtil.getColumnIndexOrThrow(_cursor, "isSurveyComplete");
          final int _cursorIndexOfFinalCombinedScore = CursorUtil.getColumnIndexOrThrow(_cursor, "finalCombinedScore");
          final int _cursorIndexOfNeedsRevisit = CursorUtil.getColumnIndexOrThrow(_cursor, "needsRevisit");
          final int _cursorIndexOfRevisitReason = CursorUtil.getColumnIndexOrThrow(_cursor, "revisitReason");
          final int _cursorIndexOfPartnerName = CursorUtil.getColumnIndexOrThrow(_cursor, "partnerName");
          final int _cursorIndexOfIsCoordinateCalibrated = CursorUtil.getColumnIndexOrThrow(_cursor, "isCoordinateCalibrated");
          final int _cursorIndexOfIsPareto = CursorUtil.getColumnIndexOrThrow(_cursor, "isPareto");
          final int _cursorIndexOfRoutePlanId = CursorUtil.getColumnIndexOrThrow(_cursor, "routePlanId");
          final int _cursorIndexOfHasSurveyedBefore = CursorUtil.getColumnIndexOrThrow(_cursor, "hasSurveyedBefore");
          final int _cursorIndexOfLastSurveyDate = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSurveyDate");
          final int _cursorIndexOfLastSurveyScore = CursorUtil.getColumnIndexOrThrow(_cursor, "lastSurveyScore");
          final List<OutletEntity> _result = new ArrayList<OutletEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final OutletEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBranchId;
            _tmpBranchId = _cursor.getLong(_cursorIndexOfBranchId);
            final String _tmpKodeToko;
            _tmpKodeToko = _cursor.getString(_cursorIndexOfKodeToko);
            final String _tmpNamaToko;
            _tmpNamaToko = _cursor.getString(_cursorIndexOfNamaToko);
            final String _tmpAlamat;
            _tmpAlamat = _cursor.getString(_cursorIndexOfAlamat);
            final Double _tmpLatitude;
            if (_cursor.isNull(_cursorIndexOfLatitude)) {
              _tmpLatitude = null;
            } else {
              _tmpLatitude = _cursor.getDouble(_cursorIndexOfLatitude);
            }
            final Double _tmpLongitude;
            if (_cursor.isNull(_cursorIndexOfLongitude)) {
              _tmpLongitude = null;
            } else {
              _tmpLongitude = _cursor.getDouble(_cursorIndexOfLongitude);
            }
            final String _tmpClassOutlet;
            _tmpClassOutlet = _cursor.getString(_cursorIndexOfClassOutlet);
            final String _tmpPriceCode;
            _tmpPriceCode = _cursor.getString(_cursorIndexOfPriceCode);
            final boolean _tmpIsSewaDisplay;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsSewaDisplay);
            _tmpIsSewaDisplay = _tmp != 0;
            final String _tmpOwnerName;
            if (_cursor.isNull(_cursorIndexOfOwnerName)) {
              _tmpOwnerName = null;
            } else {
              _tmpOwnerName = _cursor.getString(_cursorIndexOfOwnerName);
            }
            final String _tmpContactPhone;
            if (_cursor.isNull(_cursorIndexOfContactPhone)) {
              _tmpContactPhone = null;
            } else {
              _tmpContactPhone = _cursor.getString(_cursorIndexOfContactPhone);
            }
            final int _tmpStopOrder;
            _tmpStopOrder = _cursor.getInt(_cursorIndexOfStopOrder);
            final String _tmpOutletType;
            _tmpOutletType = _cursor.getString(_cursorIndexOfOutletType);
            final boolean _tmpIsVisitedByMe;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsVisitedByMe);
            _tmpIsVisitedByMe = _tmp_1 != 0;
            final boolean _tmpIsVisitedLeader;
            final int _tmp_2;
            _tmp_2 = _cursor.getInt(_cursorIndexOfIsVisitedLeader);
            _tmpIsVisitedLeader = _tmp_2 != 0;
            final boolean _tmpIsVisitedMember;
            final int _tmp_3;
            _tmp_3 = _cursor.getInt(_cursorIndexOfIsVisitedMember);
            _tmpIsVisitedMember = _tmp_3 != 0;
            final boolean _tmpIsSurveyComplete;
            final int _tmp_4;
            _tmp_4 = _cursor.getInt(_cursorIndexOfIsSurveyComplete);
            _tmpIsSurveyComplete = _tmp_4 != 0;
            final Double _tmpFinalCombinedScore;
            if (_cursor.isNull(_cursorIndexOfFinalCombinedScore)) {
              _tmpFinalCombinedScore = null;
            } else {
              _tmpFinalCombinedScore = _cursor.getDouble(_cursorIndexOfFinalCombinedScore);
            }
            final boolean _tmpNeedsRevisit;
            final int _tmp_5;
            _tmp_5 = _cursor.getInt(_cursorIndexOfNeedsRevisit);
            _tmpNeedsRevisit = _tmp_5 != 0;
            final String _tmpRevisitReason;
            if (_cursor.isNull(_cursorIndexOfRevisitReason)) {
              _tmpRevisitReason = null;
            } else {
              _tmpRevisitReason = _cursor.getString(_cursorIndexOfRevisitReason);
            }
            final String _tmpPartnerName;
            if (_cursor.isNull(_cursorIndexOfPartnerName)) {
              _tmpPartnerName = null;
            } else {
              _tmpPartnerName = _cursor.getString(_cursorIndexOfPartnerName);
            }
            final boolean _tmpIsCoordinateCalibrated;
            final int _tmp_6;
            _tmp_6 = _cursor.getInt(_cursorIndexOfIsCoordinateCalibrated);
            _tmpIsCoordinateCalibrated = _tmp_6 != 0;
            final boolean _tmpIsPareto;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsPareto);
            _tmpIsPareto = _tmp_7 != 0;
            final Long _tmpRoutePlanId;
            if (_cursor.isNull(_cursorIndexOfRoutePlanId)) {
              _tmpRoutePlanId = null;
            } else {
              _tmpRoutePlanId = _cursor.getLong(_cursorIndexOfRoutePlanId);
            }
            final boolean _tmpHasSurveyedBefore;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfHasSurveyedBefore);
            _tmpHasSurveyedBefore = _tmp_8 != 0;
            final String _tmpLastSurveyDate;
            if (_cursor.isNull(_cursorIndexOfLastSurveyDate)) {
              _tmpLastSurveyDate = null;
            } else {
              _tmpLastSurveyDate = _cursor.getString(_cursorIndexOfLastSurveyDate);
            }
            final Double _tmpLastSurveyScore;
            if (_cursor.isNull(_cursorIndexOfLastSurveyScore)) {
              _tmpLastSurveyScore = null;
            } else {
              _tmpLastSurveyScore = _cursor.getDouble(_cursorIndexOfLastSurveyScore);
            }
            _item = new OutletEntity(_tmpId,_tmpBranchId,_tmpKodeToko,_tmpNamaToko,_tmpAlamat,_tmpLatitude,_tmpLongitude,_tmpClassOutlet,_tmpPriceCode,_tmpIsSewaDisplay,_tmpOwnerName,_tmpContactPhone,_tmpStopOrder,_tmpOutletType,_tmpIsVisitedByMe,_tmpIsVisitedLeader,_tmpIsVisitedMember,_tmpIsSurveyComplete,_tmpFinalCombinedScore,_tmpNeedsRevisit,_tmpRevisitReason,_tmpPartnerName,_tmpIsCoordinateCalibrated,_tmpIsPareto,_tmpRoutePlanId,_tmpHasSurveyedBefore,_tmpLastSurveyDate,_tmpLastSurveyScore);
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

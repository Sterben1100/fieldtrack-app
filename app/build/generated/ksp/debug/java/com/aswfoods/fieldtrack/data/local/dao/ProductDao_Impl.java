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
import com.aswfoods.fieldtrack.data.local.entity.ProductEntity;
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
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class ProductDao_Impl implements ProductDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ProductEntity> __insertionAdapterOfProductEntity;

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  public ProductDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfProductEntity = new EntityInsertionAdapter<ProductEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `products` (`id`,`skuCode`,`skuName`,`category`,`isTop5`,`isNpl`,`planogramImageUrl`,`minPrice`,`maxPrice`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ProductEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindString(2, entity.getSkuCode());
        statement.bindString(3, entity.getSkuName());
        statement.bindString(4, entity.getCategory());
        final int _tmp = entity.isTop5() ? 1 : 0;
        statement.bindLong(5, _tmp);
        final int _tmp_1 = entity.isNpl() ? 1 : 0;
        statement.bindLong(6, _tmp_1);
        if (entity.getPlanogramImageUrl() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getPlanogramImageUrl());
        }
        statement.bindDouble(8, entity.getMinPrice());
        statement.bindDouble(9, entity.getMaxPrice());
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM products";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<ProductEntity> products,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfProductEntity.insert(products);
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
  public Flow<List<ProductEntity>> getAllProducts() {
    final String _sql = "SELECT * FROM products ORDER BY isTop5 DESC, isNpl DESC, id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"products"}, new Callable<List<ProductEntity>>() {
      @Override
      @NonNull
      public List<ProductEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSkuCode = CursorUtil.getColumnIndexOrThrow(_cursor, "skuCode");
          final int _cursorIndexOfSkuName = CursorUtil.getColumnIndexOrThrow(_cursor, "skuName");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIsTop5 = CursorUtil.getColumnIndexOrThrow(_cursor, "isTop5");
          final int _cursorIndexOfIsNpl = CursorUtil.getColumnIndexOrThrow(_cursor, "isNpl");
          final int _cursorIndexOfPlanogramImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "planogramImageUrl");
          final int _cursorIndexOfMinPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "minPrice");
          final int _cursorIndexOfMaxPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "maxPrice");
          final List<ProductEntity> _result = new ArrayList<ProductEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSkuCode;
            _tmpSkuCode = _cursor.getString(_cursorIndexOfSkuCode);
            final String _tmpSkuName;
            _tmpSkuName = _cursor.getString(_cursorIndexOfSkuName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final boolean _tmpIsTop5;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTop5);
            _tmpIsTop5 = _tmp != 0;
            final boolean _tmpIsNpl;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsNpl);
            _tmpIsNpl = _tmp_1 != 0;
            final String _tmpPlanogramImageUrl;
            if (_cursor.isNull(_cursorIndexOfPlanogramImageUrl)) {
              _tmpPlanogramImageUrl = null;
            } else {
              _tmpPlanogramImageUrl = _cursor.getString(_cursorIndexOfPlanogramImageUrl);
            }
            final double _tmpMinPrice;
            _tmpMinPrice = _cursor.getDouble(_cursorIndexOfMinPrice);
            final double _tmpMaxPrice;
            _tmpMaxPrice = _cursor.getDouble(_cursorIndexOfMaxPrice);
            _item = new ProductEntity(_tmpId,_tmpSkuCode,_tmpSkuName,_tmpCategory,_tmpIsTop5,_tmpIsNpl,_tmpPlanogramImageUrl,_tmpMinPrice,_tmpMaxPrice);
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
  public Object getProductsSync(final Continuation<? super List<ProductEntity>> $completion) {
    final String _sql = "SELECT * FROM products ORDER BY id ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<List<ProductEntity>>() {
      @Override
      @NonNull
      public List<ProductEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSkuCode = CursorUtil.getColumnIndexOrThrow(_cursor, "skuCode");
          final int _cursorIndexOfSkuName = CursorUtil.getColumnIndexOrThrow(_cursor, "skuName");
          final int _cursorIndexOfCategory = CursorUtil.getColumnIndexOrThrow(_cursor, "category");
          final int _cursorIndexOfIsTop5 = CursorUtil.getColumnIndexOrThrow(_cursor, "isTop5");
          final int _cursorIndexOfIsNpl = CursorUtil.getColumnIndexOrThrow(_cursor, "isNpl");
          final int _cursorIndexOfPlanogramImageUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "planogramImageUrl");
          final int _cursorIndexOfMinPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "minPrice");
          final int _cursorIndexOfMaxPrice = CursorUtil.getColumnIndexOrThrow(_cursor, "maxPrice");
          final List<ProductEntity> _result = new ArrayList<ProductEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ProductEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSkuCode;
            _tmpSkuCode = _cursor.getString(_cursorIndexOfSkuCode);
            final String _tmpSkuName;
            _tmpSkuName = _cursor.getString(_cursorIndexOfSkuName);
            final String _tmpCategory;
            _tmpCategory = _cursor.getString(_cursorIndexOfCategory);
            final boolean _tmpIsTop5;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsTop5);
            _tmpIsTop5 = _tmp != 0;
            final boolean _tmpIsNpl;
            final int _tmp_1;
            _tmp_1 = _cursor.getInt(_cursorIndexOfIsNpl);
            _tmpIsNpl = _tmp_1 != 0;
            final String _tmpPlanogramImageUrl;
            if (_cursor.isNull(_cursorIndexOfPlanogramImageUrl)) {
              _tmpPlanogramImageUrl = null;
            } else {
              _tmpPlanogramImageUrl = _cursor.getString(_cursorIndexOfPlanogramImageUrl);
            }
            final double _tmpMinPrice;
            _tmpMinPrice = _cursor.getDouble(_cursorIndexOfMinPrice);
            final double _tmpMaxPrice;
            _tmpMaxPrice = _cursor.getDouble(_cursorIndexOfMaxPrice);
            _item = new ProductEntity(_tmpId,_tmpSkuCode,_tmpSkuName,_tmpCategory,_tmpIsTop5,_tmpIsNpl,_tmpPlanogramImageUrl,_tmpMinPrice,_tmpMaxPrice);
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

package com.aswfoods.fieldtrack.data.local.database;

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
import com.aswfoods.fieldtrack.data.local.dao.OutletDao;
import com.aswfoods.fieldtrack.data.local.dao.OutletDao_Impl;
import com.aswfoods.fieldtrack.data.local.dao.PendingVisitDao;
import com.aswfoods.fieldtrack.data.local.dao.PendingVisitDao_Impl;
import com.aswfoods.fieldtrack.data.local.dao.ProductDao;
import com.aswfoods.fieldtrack.data.local.dao.ProductDao_Impl;
import com.aswfoods.fieldtrack.data.local.dao.SupervisorDao;
import com.aswfoods.fieldtrack.data.local.dao.SupervisorDao_Impl;
import com.aswfoods.fieldtrack.data.local.dao.SurveyQuestionDao;
import com.aswfoods.fieldtrack.data.local.dao.SurveyQuestionDao_Impl;
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
  private volatile OutletDao _outletDao;

  private volatile ProductDao _productDao;

  private volatile SupervisorDao _supervisorDao;

  private volatile PendingVisitDao _pendingVisitDao;

  private volatile SurveyQuestionDao _surveyQuestionDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(6) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `outlets` (`id` INTEGER NOT NULL, `branchId` INTEGER NOT NULL, `kodeToko` TEXT NOT NULL, `namaToko` TEXT NOT NULL, `alamat` TEXT NOT NULL, `latitude` REAL, `longitude` REAL, `classOutlet` TEXT NOT NULL, `priceCode` TEXT NOT NULL, `isSewaDisplay` INTEGER NOT NULL, `ownerName` TEXT, `contactPhone` TEXT, `stopOrder` INTEGER NOT NULL, `outletType` TEXT NOT NULL, `isVisitedByMe` INTEGER NOT NULL, `isVisitedLeader` INTEGER NOT NULL, `isVisitedMember` INTEGER NOT NULL, `isSurveyComplete` INTEGER NOT NULL, `finalCombinedScore` REAL, `needsRevisit` INTEGER NOT NULL, `revisitReason` TEXT, `partnerName` TEXT, `isCoordinateCalibrated` INTEGER NOT NULL, `isPareto` INTEGER NOT NULL, `routePlanId` INTEGER, `hasSurveyedBefore` INTEGER NOT NULL, `lastSurveyDate` TEXT, `lastSurveyScore` REAL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `products` (`id` INTEGER NOT NULL, `skuCode` TEXT NOT NULL, `skuName` TEXT NOT NULL, `category` TEXT NOT NULL, `isTop5` INTEGER NOT NULL, `isNpl` INTEGER NOT NULL, `planogramImageUrl` TEXT, `minPrice` REAL NOT NULL, `maxPrice` REAL NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `supervisors` (`id` INTEGER NOT NULL, `salesCode` TEXT NOT NULL, `nip` TEXT NOT NULL, `name` TEXT NOT NULL, `areaName` TEXT, `coverageDistributors` TEXT, `photoUrl` TEXT NOT NULL, `isTarget` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `pending_visits` (`offlineSyncId` TEXT NOT NULL, `outletId` INTEGER NOT NULL, `outletName` TEXT NOT NULL, `roleType` TEXT NOT NULL, `visitDate` TEXT NOT NULL, `checkinLatitude` REAL, `checkinLongitude` REAL, `checkinAt` TEXT, `checkoutAt` TEXT, `payloadJson` TEXT NOT NULL, `syncStatus` INTEGER NOT NULL, `errorMessage` TEXT, `isIncomplete` INTEGER NOT NULL, `needsRevisit` INTEGER NOT NULL, `revisitReason` TEXT, `localScore` REAL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`offlineSyncId`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `survey_questions` (`id` INTEGER NOT NULL, `role` TEXT NOT NULL, `questionCode` TEXT NOT NULL, `aspect` TEXT NOT NULL, `questionText` TEXT NOT NULL, `answerType` TEXT NOT NULL, `scoringGuide` TEXT, `weight` REAL NOT NULL, `isActive` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'eaa2aaeacbdafab156fc2f2163e4ea5c')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `outlets`");
        db.execSQL("DROP TABLE IF EXISTS `products`");
        db.execSQL("DROP TABLE IF EXISTS `supervisors`");
        db.execSQL("DROP TABLE IF EXISTS `pending_visits`");
        db.execSQL("DROP TABLE IF EXISTS `survey_questions`");
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
        final HashMap<String, TableInfo.Column> _columnsOutlets = new HashMap<String, TableInfo.Column>(28);
        _columnsOutlets.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("branchId", new TableInfo.Column("branchId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("kodeToko", new TableInfo.Column("kodeToko", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("namaToko", new TableInfo.Column("namaToko", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("alamat", new TableInfo.Column("alamat", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("latitude", new TableInfo.Column("latitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("longitude", new TableInfo.Column("longitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("classOutlet", new TableInfo.Column("classOutlet", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("priceCode", new TableInfo.Column("priceCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("isSewaDisplay", new TableInfo.Column("isSewaDisplay", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("ownerName", new TableInfo.Column("ownerName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("contactPhone", new TableInfo.Column("contactPhone", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("stopOrder", new TableInfo.Column("stopOrder", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("outletType", new TableInfo.Column("outletType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("isVisitedByMe", new TableInfo.Column("isVisitedByMe", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("isVisitedLeader", new TableInfo.Column("isVisitedLeader", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("isVisitedMember", new TableInfo.Column("isVisitedMember", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("isSurveyComplete", new TableInfo.Column("isSurveyComplete", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("finalCombinedScore", new TableInfo.Column("finalCombinedScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("needsRevisit", new TableInfo.Column("needsRevisit", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("revisitReason", new TableInfo.Column("revisitReason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("partnerName", new TableInfo.Column("partnerName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("isCoordinateCalibrated", new TableInfo.Column("isCoordinateCalibrated", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("isPareto", new TableInfo.Column("isPareto", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("routePlanId", new TableInfo.Column("routePlanId", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("hasSurveyedBefore", new TableInfo.Column("hasSurveyedBefore", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("lastSurveyDate", new TableInfo.Column("lastSurveyDate", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsOutlets.put("lastSurveyScore", new TableInfo.Column("lastSurveyScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysOutlets = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesOutlets = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoOutlets = new TableInfo("outlets", _columnsOutlets, _foreignKeysOutlets, _indicesOutlets);
        final TableInfo _existingOutlets = TableInfo.read(db, "outlets");
        if (!_infoOutlets.equals(_existingOutlets)) {
          return new RoomOpenHelper.ValidationResult(false, "outlets(com.aswfoods.fieldtrack.data.local.entity.OutletEntity).\n"
                  + " Expected:\n" + _infoOutlets + "\n"
                  + " Found:\n" + _existingOutlets);
        }
        final HashMap<String, TableInfo.Column> _columnsProducts = new HashMap<String, TableInfo.Column>(9);
        _columnsProducts.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("skuCode", new TableInfo.Column("skuCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("skuName", new TableInfo.Column("skuName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("category", new TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("isTop5", new TableInfo.Column("isTop5", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("isNpl", new TableInfo.Column("isNpl", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("planogramImageUrl", new TableInfo.Column("planogramImageUrl", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("minPrice", new TableInfo.Column("minPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsProducts.put("maxPrice", new TableInfo.Column("maxPrice", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysProducts = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesProducts = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoProducts = new TableInfo("products", _columnsProducts, _foreignKeysProducts, _indicesProducts);
        final TableInfo _existingProducts = TableInfo.read(db, "products");
        if (!_infoProducts.equals(_existingProducts)) {
          return new RoomOpenHelper.ValidationResult(false, "products(com.aswfoods.fieldtrack.data.local.entity.ProductEntity).\n"
                  + " Expected:\n" + _infoProducts + "\n"
                  + " Found:\n" + _existingProducts);
        }
        final HashMap<String, TableInfo.Column> _columnsSupervisors = new HashMap<String, TableInfo.Column>(8);
        _columnsSupervisors.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSupervisors.put("salesCode", new TableInfo.Column("salesCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSupervisors.put("nip", new TableInfo.Column("nip", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSupervisors.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSupervisors.put("areaName", new TableInfo.Column("areaName", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSupervisors.put("coverageDistributors", new TableInfo.Column("coverageDistributors", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSupervisors.put("photoUrl", new TableInfo.Column("photoUrl", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSupervisors.put("isTarget", new TableInfo.Column("isTarget", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSupervisors = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSupervisors = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSupervisors = new TableInfo("supervisors", _columnsSupervisors, _foreignKeysSupervisors, _indicesSupervisors);
        final TableInfo _existingSupervisors = TableInfo.read(db, "supervisors");
        if (!_infoSupervisors.equals(_existingSupervisors)) {
          return new RoomOpenHelper.ValidationResult(false, "supervisors(com.aswfoods.fieldtrack.data.local.entity.SupervisorEntity).\n"
                  + " Expected:\n" + _infoSupervisors + "\n"
                  + " Found:\n" + _existingSupervisors);
        }
        final HashMap<String, TableInfo.Column> _columnsPendingVisits = new HashMap<String, TableInfo.Column>(17);
        _columnsPendingVisits.put("offlineSyncId", new TableInfo.Column("offlineSyncId", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("outletId", new TableInfo.Column("outletId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("outletName", new TableInfo.Column("outletName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("roleType", new TableInfo.Column("roleType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("visitDate", new TableInfo.Column("visitDate", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("checkinLatitude", new TableInfo.Column("checkinLatitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("checkinLongitude", new TableInfo.Column("checkinLongitude", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("checkinAt", new TableInfo.Column("checkinAt", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("checkoutAt", new TableInfo.Column("checkoutAt", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("payloadJson", new TableInfo.Column("payloadJson", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("syncStatus", new TableInfo.Column("syncStatus", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("errorMessage", new TableInfo.Column("errorMessage", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("isIncomplete", new TableInfo.Column("isIncomplete", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("needsRevisit", new TableInfo.Column("needsRevisit", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("revisitReason", new TableInfo.Column("revisitReason", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("localScore", new TableInfo.Column("localScore", "REAL", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsPendingVisits.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysPendingVisits = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesPendingVisits = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoPendingVisits = new TableInfo("pending_visits", _columnsPendingVisits, _foreignKeysPendingVisits, _indicesPendingVisits);
        final TableInfo _existingPendingVisits = TableInfo.read(db, "pending_visits");
        if (!_infoPendingVisits.equals(_existingPendingVisits)) {
          return new RoomOpenHelper.ValidationResult(false, "pending_visits(com.aswfoods.fieldtrack.data.local.entity.PendingVisitEntity).\n"
                  + " Expected:\n" + _infoPendingVisits + "\n"
                  + " Found:\n" + _existingPendingVisits);
        }
        final HashMap<String, TableInfo.Column> _columnsSurveyQuestions = new HashMap<String, TableInfo.Column>(9);
        _columnsSurveyQuestions.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyQuestions.put("role", new TableInfo.Column("role", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyQuestions.put("questionCode", new TableInfo.Column("questionCode", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyQuestions.put("aspect", new TableInfo.Column("aspect", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyQuestions.put("questionText", new TableInfo.Column("questionText", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyQuestions.put("answerType", new TableInfo.Column("answerType", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyQuestions.put("scoringGuide", new TableInfo.Column("scoringGuide", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyQuestions.put("weight", new TableInfo.Column("weight", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSurveyQuestions.put("isActive", new TableInfo.Column("isActive", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSurveyQuestions = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSurveyQuestions = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSurveyQuestions = new TableInfo("survey_questions", _columnsSurveyQuestions, _foreignKeysSurveyQuestions, _indicesSurveyQuestions);
        final TableInfo _existingSurveyQuestions = TableInfo.read(db, "survey_questions");
        if (!_infoSurveyQuestions.equals(_existingSurveyQuestions)) {
          return new RoomOpenHelper.ValidationResult(false, "survey_questions(com.aswfoods.fieldtrack.data.local.entity.SurveyQuestionEntity).\n"
                  + " Expected:\n" + _infoSurveyQuestions + "\n"
                  + " Found:\n" + _existingSurveyQuestions);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "eaa2aaeacbdafab156fc2f2163e4ea5c", "454e749a4c56697771e795451c979823");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "outlets","products","supervisors","pending_visits","survey_questions");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `outlets`");
      _db.execSQL("DELETE FROM `products`");
      _db.execSQL("DELETE FROM `supervisors`");
      _db.execSQL("DELETE FROM `pending_visits`");
      _db.execSQL("DELETE FROM `survey_questions`");
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
    _typeConvertersMap.put(OutletDao.class, OutletDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ProductDao.class, ProductDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SupervisorDao.class, SupervisorDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(PendingVisitDao.class, PendingVisitDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SurveyQuestionDao.class, SurveyQuestionDao_Impl.getRequiredConverters());
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
  public OutletDao outletDao() {
    if (_outletDao != null) {
      return _outletDao;
    } else {
      synchronized(this) {
        if(_outletDao == null) {
          _outletDao = new OutletDao_Impl(this);
        }
        return _outletDao;
      }
    }
  }

  @Override
  public ProductDao productDao() {
    if (_productDao != null) {
      return _productDao;
    } else {
      synchronized(this) {
        if(_productDao == null) {
          _productDao = new ProductDao_Impl(this);
        }
        return _productDao;
      }
    }
  }

  @Override
  public SupervisorDao supervisorDao() {
    if (_supervisorDao != null) {
      return _supervisorDao;
    } else {
      synchronized(this) {
        if(_supervisorDao == null) {
          _supervisorDao = new SupervisorDao_Impl(this);
        }
        return _supervisorDao;
      }
    }
  }

  @Override
  public PendingVisitDao pendingVisitDao() {
    if (_pendingVisitDao != null) {
      return _pendingVisitDao;
    } else {
      synchronized(this) {
        if(_pendingVisitDao == null) {
          _pendingVisitDao = new PendingVisitDao_Impl(this);
        }
        return _pendingVisitDao;
      }
    }
  }

  @Override
  public SurveyQuestionDao surveyQuestionDao() {
    if (_surveyQuestionDao != null) {
      return _surveyQuestionDao;
    } else {
      synchronized(this) {
        if(_surveyQuestionDao == null) {
          _surveyQuestionDao = new SurveyQuestionDao_Impl(this);
        }
        return _surveyQuestionDao;
      }
    }
  }
}

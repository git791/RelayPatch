package dev.relaypatch.app.data.db;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import dev.relaypatch.app.data.model.Patch;
import dev.relaypatch.app.data.model.PatchStatus;
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
public final class PatchDao_Impl implements PatchDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<Patch> __insertionAdapterOfPatch;

  private final PatchStatusConverter __patchStatusConverter = new PatchStatusConverter();

  private final EntityDeletionOrUpdateAdapter<Patch> __updateAdapterOfPatch;

  private final SharedSQLiteStatement __preparedStmtOfUpdateStatus;

  public PatchDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfPatch = new EntityInsertionAdapter<Patch>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `patches` (`id`,`createdAt`,`errorText`,`spokenIntent`,`diffText`,`status`,`targetFileHint`) VALUES (?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Patch entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getCreatedAt());
        statement.bindString(3, entity.getErrorText());
        statement.bindString(4, entity.getSpokenIntent());
        statement.bindString(5, entity.getDiffText());
        final String _tmp = __patchStatusConverter.fromPatchStatus(entity.getStatus());
        statement.bindString(6, _tmp);
        if (entity.getTargetFileHint() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTargetFileHint());
        }
      }
    };
    this.__updateAdapterOfPatch = new EntityDeletionOrUpdateAdapter<Patch>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "UPDATE OR ABORT `patches` SET `id` = ?,`createdAt` = ?,`errorText` = ?,`spokenIntent` = ?,`diffText` = ?,`status` = ?,`targetFileHint` = ? WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final Patch entity) {
        statement.bindString(1, entity.getId());
        statement.bindLong(2, entity.getCreatedAt());
        statement.bindString(3, entity.getErrorText());
        statement.bindString(4, entity.getSpokenIntent());
        statement.bindString(5, entity.getDiffText());
        final String _tmp = __patchStatusConverter.fromPatchStatus(entity.getStatus());
        statement.bindString(6, _tmp);
        if (entity.getTargetFileHint() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getTargetFileHint());
        }
        statement.bindString(8, entity.getId());
      }
    };
    this.__preparedStmtOfUpdateStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE patches SET status = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertPatch(final Patch patch, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfPatch.insert(patch);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updatePatch(final Patch patch, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __updateAdapterOfPatch.handle(patch);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateStatus(final String id, final PatchStatus status,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateStatus.acquire();
        int _argIndex = 1;
        final String _tmp = __patchStatusConverter.fromPatchStatus(status);
        _stmt.bindString(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
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
          __preparedStmtOfUpdateStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<Patch>> getAllPatches() {
    final String _sql = "SELECT * FROM patches ORDER BY createdAt DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"patches"}, new Callable<List<Patch>>() {
      @Override
      @NonNull
      public List<Patch> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfErrorText = CursorUtil.getColumnIndexOrThrow(_cursor, "errorText");
          final int _cursorIndexOfSpokenIntent = CursorUtil.getColumnIndexOrThrow(_cursor, "spokenIntent");
          final int _cursorIndexOfDiffText = CursorUtil.getColumnIndexOrThrow(_cursor, "diffText");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTargetFileHint = CursorUtil.getColumnIndexOrThrow(_cursor, "targetFileHint");
          final List<Patch> _result = new ArrayList<Patch>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Patch _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpErrorText;
            _tmpErrorText = _cursor.getString(_cursorIndexOfErrorText);
            final String _tmpSpokenIntent;
            _tmpSpokenIntent = _cursor.getString(_cursorIndexOfSpokenIntent);
            final String _tmpDiffText;
            _tmpDiffText = _cursor.getString(_cursorIndexOfDiffText);
            final PatchStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __patchStatusConverter.toPatchStatus(_tmp);
            final String _tmpTargetFileHint;
            if (_cursor.isNull(_cursorIndexOfTargetFileHint)) {
              _tmpTargetFileHint = null;
            } else {
              _tmpTargetFileHint = _cursor.getString(_cursorIndexOfTargetFileHint);
            }
            _item = new Patch(_tmpId,_tmpCreatedAt,_tmpErrorText,_tmpSpokenIntent,_tmpDiffText,_tmpStatus,_tmpTargetFileHint);
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
  public Object getPatchById(final String id, final Continuation<? super Patch> $completion) {
    final String _sql = "SELECT * FROM patches WHERE id = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, id);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Patch>() {
      @Override
      @Nullable
      public Patch call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfErrorText = CursorUtil.getColumnIndexOrThrow(_cursor, "errorText");
          final int _cursorIndexOfSpokenIntent = CursorUtil.getColumnIndexOrThrow(_cursor, "spokenIntent");
          final int _cursorIndexOfDiffText = CursorUtil.getColumnIndexOrThrow(_cursor, "diffText");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTargetFileHint = CursorUtil.getColumnIndexOrThrow(_cursor, "targetFileHint");
          final Patch _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpErrorText;
            _tmpErrorText = _cursor.getString(_cursorIndexOfErrorText);
            final String _tmpSpokenIntent;
            _tmpSpokenIntent = _cursor.getString(_cursorIndexOfSpokenIntent);
            final String _tmpDiffText;
            _tmpDiffText = _cursor.getString(_cursorIndexOfDiffText);
            final PatchStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __patchStatusConverter.toPatchStatus(_tmp);
            final String _tmpTargetFileHint;
            if (_cursor.isNull(_cursorIndexOfTargetFileHint)) {
              _tmpTargetFileHint = null;
            } else {
              _tmpTargetFileHint = _cursor.getString(_cursorIndexOfTargetFileHint);
            }
            _result = new Patch(_tmpId,_tmpCreatedAt,_tmpErrorText,_tmpSpokenIntent,_tmpDiffText,_tmpStatus,_tmpTargetFileHint);
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
  public Flow<List<Patch>> getReadyPatches() {
    final String _sql = "SELECT * FROM patches WHERE status = 'READY' ORDER BY createdAt ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"patches"}, new Callable<List<Patch>>() {
      @Override
      @NonNull
      public List<Patch> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCreatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "createdAt");
          final int _cursorIndexOfErrorText = CursorUtil.getColumnIndexOrThrow(_cursor, "errorText");
          final int _cursorIndexOfSpokenIntent = CursorUtil.getColumnIndexOrThrow(_cursor, "spokenIntent");
          final int _cursorIndexOfDiffText = CursorUtil.getColumnIndexOrThrow(_cursor, "diffText");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfTargetFileHint = CursorUtil.getColumnIndexOrThrow(_cursor, "targetFileHint");
          final List<Patch> _result = new ArrayList<Patch>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final Patch _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final long _tmpCreatedAt;
            _tmpCreatedAt = _cursor.getLong(_cursorIndexOfCreatedAt);
            final String _tmpErrorText;
            _tmpErrorText = _cursor.getString(_cursorIndexOfErrorText);
            final String _tmpSpokenIntent;
            _tmpSpokenIntent = _cursor.getString(_cursorIndexOfSpokenIntent);
            final String _tmpDiffText;
            _tmpDiffText = _cursor.getString(_cursorIndexOfDiffText);
            final PatchStatus _tmpStatus;
            final String _tmp;
            _tmp = _cursor.getString(_cursorIndexOfStatus);
            _tmpStatus = __patchStatusConverter.toPatchStatus(_tmp);
            final String _tmpTargetFileHint;
            if (_cursor.isNull(_cursorIndexOfTargetFileHint)) {
              _tmpTargetFileHint = null;
            } else {
              _tmpTargetFileHint = _cursor.getString(_cursorIndexOfTargetFileHint);
            }
            _item = new Patch(_tmpId,_tmpCreatedAt,_tmpErrorText,_tmpSpokenIntent,_tmpDiffText,_tmpStatus,_tmpTargetFileHint);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}

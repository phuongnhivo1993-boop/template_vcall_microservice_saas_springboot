package com.vcall.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class BulkOperationUtil {

    private BulkOperationUtil() {}

    public static <T> BulkResult<T> bulkDelete(List<UUID> ids, java.util.function.Function<UUID, T> deleteFn) {
        BulkResult<T> result = new BulkResult<>();
        for (UUID id : ids) {
            try {
                deleteFn.apply(id);
                result.addSuccess(id);
            } catch (Exception e) {
                result.addFailure(id, e.getMessage());
            }
        }
        return result;
    }

    public static <T> BulkResult<T> bulkCreate(List<T> items, Consumer<T> createFn) {
        BulkResult<T> result = new BulkResult<>();
        for (T item : items) {
            try {
                createFn.accept(item);
                result.addSuccess(item);
            } catch (Exception e) {
                result.addFailure(item, e.getMessage());
            }
        }
        return result;
    }

    public static class BulkResult<T> {
        private final List<Object> successItems = new ArrayList<>();
        private final List<BulkError> errors = new ArrayList<>();

        public void addSuccess(Object item) { successItems.add(item); }
        public void addFailure(Object item, String error) { errors.add(new BulkError(String.valueOf(item), error)); }

        public int getSuccessCount() { return successItems.size(); }
        public int getFailureCount() { return errors.size(); }
        public List<Object> getSuccessItems() { return successItems; }
        public List<BulkError> getErrors() { return errors; }

        public record BulkError(String item, String message) {}
    }
}

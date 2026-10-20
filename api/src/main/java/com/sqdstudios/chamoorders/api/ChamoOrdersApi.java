package com.sqdstudios.chamoorders.api;

import com.sqdstudios.chamoorders.api.database.DatabaseManager;
import org.jspecify.annotations.NonNull;

public interface ChamoOrdersApi {
    @NonNull DatabaseManager getDatabaseManager();
}

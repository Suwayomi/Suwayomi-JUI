/*
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package ca.gosyer.jui.ui.base.state

import android.os.Bundle
import androidx.annotation.MainThread
import androidx.lifecycle.DEFAULT_ARGS_KEY
import androidx.lifecycle.SAVED_STATE_REGISTRY_OWNER_KEY
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.lifecycle.ViewModelProvider.NewInstanceFactory.Companion.VIEW_MODEL_KEY
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.enableSavedStateHandles
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.MutableCreationExtras
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.screen.Screen
import java.lang.reflect.Method

private const val SAVED_STATE_KEY = "androidx.lifecycle.internal.SavedStateHandlesProvider"

val getSavedStateHandlesVM: Method by lazy {
    Class.forName("androidx.lifecycle.SavedStateHandleSupport")
        .methods
        .first { it.name == "getSavedStateHandlesVM" }
}

private fun createSavedStateHandle(
    savedStateRegistryOwner: SavedStateRegistryOwner,
    viewModelStoreOwner: ViewModelStoreOwner,
    key: String,
    defaultArgs: Bundle?,
): SavedStateHandle {
    val provider = savedStateRegistryOwner.savedStateHandlesProvider
    // If we already have a reference to a previously created SavedStateHandle
    // for a given key stored in our ViewModel, use that. Otherwise, create
    // a new SavedStateHandle, providing it any restored state we might have saved
    val vm = getSavedStateHandlesVM.invoke(null, viewModelStoreOwner)!!

    @Suppress("UNCHECKED_CAST")
    val handles = vm::class.java
        .methods
        .first { it.name == "getHandles" }
        .invoke(vm) as MutableMap<String, SavedStateHandle>

    return handles[key] ?: SavedStateHandle.createHandle(
        provider.consumeRestoredStateForKey(key),
        defaultArgs,
    ).also { handles[key] = it }
}

/**
 * Creates `SavedStateHandle` that can be used in your ViewModels
 *
 * This function requires [enableSavedStateHandles] call during the component
 * initialization. Latest versions of androidx components like `ComponentActivity`, `Fragment`,
 * `NavBackStackEntry` makes this call automatically.
 *
 * This [CreationExtras] must contain [SAVED_STATE_REGISTRY_OWNER_KEY],
 * [VIEW_MODEL_STORE_OWNER_KEY] and [VIEW_MODEL_KEY].
 *
 * @throws IllegalArgumentException if this `CreationExtras` are missing required keys:
 * `ViewModelStoreOwnerKey`, `SavedStateRegistryOwnerKey`, `VIEW_MODEL_KEY`
 */
@MainThread
fun CreationExtras.createSavedStateHandle(): SavedStateHandle {
    val savedStateRegistryOwner = this[SAVED_STATE_REGISTRY_OWNER_KEY]
        ?: throw IllegalArgumentException(
            "CreationExtras must have a value by `SAVED_STATE_REGISTRY_OWNER_KEY`",
        )
    val viewModelStateRegistryOwner = this[VIEW_MODEL_STORE_OWNER_KEY]
        ?: throw IllegalArgumentException(
            "CreationExtras must have a value by `VIEW_MODEL_STORE_OWNER_KEY`",
        )
    val defaultArgs = this[DEFAULT_ARGS_KEY]
    val key = this[VIEW_MODEL_KEY] ?: throw IllegalArgumentException(
        "CreationExtras must have a value by `VIEW_MODEL_KEY`",
    )
    return createSavedStateHandle(
        savedStateRegistryOwner,
        viewModelStateRegistryOwner,
        key,
        defaultArgs,
    )
}

internal val SavedStateRegistryOwner.savedStateHandlesProvider: SavedStateHandlesProvider
    get() = savedStateRegistry.getSavedStateProvider(SAVED_STATE_KEY)?.let(::SavedStateHandlesProvider)
        ?: throw IllegalStateException(
            "enableSavedStateHandles() wasn't called " +
                "prior to createSavedStateHandle() call",
        )

/**
 * This single SavedStateProvider is responsible for saving the state of every
 * SavedStateHandle associated with the SavedState/ViewModel pair.
 */
internal class SavedStateHandlesProvider(
    private val savedStateRegistry: SavedStateRegistry.SavedStateProvider,
) {
    /**
     * Restore the state associated with a particular SavedStateHandle, identified by its [key]
     */
    fun consumeRestoredStateForKey(key: String): Bundle? {
        return savedStateRegistry::class.java
            .methods
            .find { it.name == "consumeRestoredStateForKey" }
            ?.invoke(savedStateRegistry, key) as? Bundle
    }
}

inline fun <reified T : ScreenModel> CreationExtras.addScreenModelKey(
    screen: Screen,
    tag: String?,
): CreationExtras {
    return MutableCreationExtras(this).apply {
        set(
            VIEW_MODEL_KEY,
            "${screen.key}:${T::class.qualifiedName}:${tag ?: "default"}",
        )
    }
}

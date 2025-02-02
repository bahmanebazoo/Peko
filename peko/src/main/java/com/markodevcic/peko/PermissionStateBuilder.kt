package com.markodevcic.peko

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

internal interface PermissionStateBuilder {
	fun createPermissionState(context: Context, vararg permissions: String): PermissionState

	private class PekoPermissionStateBuilder : PermissionStateBuilder {
		override fun createPermissionState(context: Context, vararg permissions: String): PermissionState {
			val permissionsGroup = permissions.groupBy { p -> ContextCompat.checkSelfPermission(context, p) }
			val denied = permissionsGroup[PackageManager.PERMISSION_DENIED] ?: listOf()
			val granted = permissionsGroup[PackageManager.PERMISSION_GRANTED] ?: listOf()
			return PermissionState(granted, denied)
		}

	}

	companion object {
		fun default(): PermissionStateBuilder = PekoPermissionStateBuilder()
	}
}
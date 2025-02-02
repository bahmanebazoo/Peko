package com.markodevcic.peko

import android.content.Context
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito

class PermissionRequesterTest {
	private val activityFactory = Mockito.mock(NativeActivityFactory::class.java)
	private val context = Mockito.mock(Context::class.java)
	private val nativeActivity = Mockito.mock(NativeActivity::class.java)
	private val permissionStateBuilder = Mockito.mock(PermissionStateBuilder::class.java)

	private lateinit var permissionChannel: Channel<PermissionResult>

	private lateinit var sut: PermissionRequester

	@Before
	fun setup() {
		permissionChannel = Channel()

		PermissionRequester.activityFactory = activityFactory
		PermissionRequester.permissionStateBuilder = permissionStateBuilder
		PermissionRequester.initialize(context)

		Mockito.`when`(activityFactory.getActivityAsync(context)).thenReturn(CompletableDeferred(nativeActivity))

		sut = PermissionRequester.instance()
	}

	@Test
	fun testGranted() {
		val permission = "CONTACTS"

		Mockito.`when`(permissionStateBuilder.createPermissionState(context, permission)).thenReturn(
			PermissionState(
				listOf(), listOf(
					permission
				)
			)
		)


		runBlocking {
			launch {
				delay(200)
				permissionChannel.send(PermissionResult.Granted(permission))
				permissionChannel.close()
			}
			Assert.assertTrue(sut.request(permission).allGranted())
		}
	}

	@Test
	fun testAlreadyGranted() {
		val permission = "CONTACTS"

		Mockito.`when`(permissionStateBuilder.createPermissionState(context, permission)).thenReturn(
			PermissionState(
				listOf(permission), listOf()
			)
		)


		runBlocking {
			Assert.assertTrue(sut.request(permission).allGranted())
		}
	}

	@Test
	fun testAnyGranted() {
		val denied = "COARSE_LOCATION"
		val granted = "FUSED_LOCATION"

		Mockito.`when`(permissionStateBuilder.createPermissionState(context, denied, granted)).thenReturn(
			PermissionState(
				granted = listOf(granted),
				denied = listOf(denied)
			)
		)

		val isAnyGranted = sut.isAnyGranted(denied, granted)

		Assert.assertTrue(isAnyGranted)
	}

	@Test
	fun testAreGranted() {
		val denied = "COARSE_LOCATION"
		val granted = "FUSED_LOCATION"

		Mockito.`when`(permissionStateBuilder.createPermissionState(context, denied, granted)).thenReturn(
			PermissionState(
				granted = listOf(granted),
				denied = listOf(denied)
			)
		)

		val areGranted = sut.areGranted(denied, granted)

		Assert.assertFalse(areGranted)
	}
}
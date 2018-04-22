package com.jakdor.iotkettle

import android.content.SharedPreferences
import com.jakdor.iotkettle.main.MainActivity
import com.jakdor.iotkettle.main.MainPresenter
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.junit.runner.RunWith
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class MainActivityTest {

    @get:Rule
    var thrown = ExpectedException.none()

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()

    private lateinit var presenter: MainPresenter
    private lateinit var preferences: SharedPreferences
    private lateinit var mainActivity: MainActivity

    private val testSavedIp = "192.168.1.42"

    @Before
    fun setUp(){
        presenter = mock {}

        preferences = mock {
            on {getString(Mockito.anyString(), Mockito.anyString())} doReturn testSavedIp
        }

        mainActivity = Robolectric.setupActivity(MainActivity::class.java)
        mainActivity.presenter = presenter
        mainActivity.preferences = preferences
    }

    @Test
    fun setupTest(){
        Assert.assertNotNull(mainActivity)
        Assert.assertNotNull(presenter)
        Assert.assertNotNull(mainActivity.presenter)
        Assert.assertEquals(presenter, mainActivity.presenter)
    }

    @Test
    fun loadIpTest(){
        mainActivity.loadIp()
        verify(preferences).getString(Mockito.anyString(), Mockito.anyString())
        verify(presenter).connectionString = testSavedIp
    }


}
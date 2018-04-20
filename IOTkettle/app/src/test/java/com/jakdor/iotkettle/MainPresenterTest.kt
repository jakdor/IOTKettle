package com.jakdor.iotkettle

import com.jakdor.iotkettle.main.MainActivity
import com.jakdor.iotkettle.main.MainPresenter
import com.nhaarman.mockito_kotlin.*
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
import org.mockito.InOrder
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnit

class MainPresenterTest {

    @get:Rule
    var thrown = ExpectedException.none()!!

    @get:Rule
    var mockitoRule = MockitoJUnit.rule()

    private val viewIpEditTestStr = TestUtils.randomString()

    private var view: MainActivity = mock {
        on { getIpEditText() } doReturn viewIpEditTestStr
    }

    private val mainPresenter: MainPresenter = MainPresenter(view)

    @Test
    fun onIpChangedTest(){
        mainPresenter.connectionString = TestUtils.randomString()

        mainPresenter.onIpChanged()

        verify(view).saveIp(viewIpEditTestStr)
        verify(view).changeServiceIp(mainPresenter.connectionString)
        Assert.assertEquals(viewIpEditTestStr, mainPresenter.connectionString)
    }

    @Test
    fun connectedTest(){
        mainPresenter.connected()

        verify(view).setStatusTextView(R.string.status_connected)
    }

    @Test
    fun connectingTest(){
        mainPresenter.connecting()

        verify(view).setStatusTextView(R.string.status_connecting)
    }

    @Test
    fun disconnectTest(){
        mainPresenter.disconnect()

        verify(view).setStatusTextView(R.string.status_no_connection)
    }

    @Test
    fun userDisconnectTest(){
        mainPresenter.userDisconnect()

        val inOrder: InOrder = inOrder(view)
        inOrder.verify(view).stopService()
        inOrder.verify(view).setStatusTextView(R.string.status_no_connection)
        inOrder.verify(view).stopTimer()
        inOrder.verify(view).setTimerDisplayTextView("")
    }

    /**
     * test [MainPresenter.receive] for true start parameter
     */
    @Test
    fun receiveTrueTest(){
        mainPresenter.receive(true)

        verify(view).startTimer()
        verify(view).setStatusTextView(R.string.status_working)
        Assert.assertTrue(mainPresenter.timerFlag)
    }

    /**
     * test [MainPresenter.receive] for false start parameter
     */
    @Test
    fun receiveFalseTest(){
        mainPresenter.receive(false)

        verify(view).stopTimer()
        verify(view).setStatusTextView(R.string.status_ended)
        Assert.assertFalse(mainPresenter.timerFlag)
    }

    /**
     * test [MainPresenter.timeCounter] for true timerFrag variable
     */
    @Test
    fun timerCounterTrueTest(){
        mainPresenter.timerFlag = true

        mainPresenter.timeCounter()

        Assert.assertNotEquals(0L, mainPresenter.timerStart)
        verify(view).setTimerDisplayTextView(Mockito.anyString())
    }

    /**
     * test [MainPresenter.timeCounter] for false timerFrag variable
     */
    @Test
    fun timerCounterFalseTest(){
        mainPresenter.timerFlag = false

        mainPresenter.timeCounter()

        verify(view, times(0)).setTimerDisplayTextView(Mockito.anyString())
    }

    /**
     * [MainPresenter.timeCounter] integration test
     */
    @Test
    fun timerCounterTest(){
        mainPresenter.timerFlag = true

        mainPresenter.timeCounter()
        Thread.sleep(1000)
        mainPresenter.timeCounter() //run twice to set value to timer variable
        mainPresenter.timerFlag = false
        mainPresenter.timeCounter()

        verify(view, times(3)).setTimerDisplayTextView(Mockito.anyString())
    }
}

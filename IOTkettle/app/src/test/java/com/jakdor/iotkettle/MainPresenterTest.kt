package com.jakdor.iotkettle

import com.jakdor.iotkettle.main.MainActivity
import com.jakdor.iotkettle.main.MainPresenter
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException
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
}

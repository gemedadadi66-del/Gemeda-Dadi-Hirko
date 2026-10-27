package com.example

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.User
import com.example.domain.processor.DocumentImageProcessor
import com.example.util.SecurityUtils
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Waraqaa Gulaalaa", appName)
    }

    @Test
    fun `test stamp crop and relocation`() {
        val source = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        for (x in 0 until 200) {
            for (y in 0 until 200) {
                source.setPixel(x, y, Color.WHITE)
            }
        }
        for (x in 120 until 160) {
            for (y in 120 until 160) {
                source.setPixel(x, y, Color.RED)
            }
        }

        val edited = DocumentImageProcessor.cropAndRelocateStamp(
            sourceBitmap = source,
            cropX = 120,
            cropY = 120,
            cropW = 40,
            cropH = 40,
            newX = 20,
            newY = 20,
            newW = 50,
            newH = 50,
            matchPaperBackground = true
        )

        assertNotNull(edited)
        assertEquals(200, edited.width)
        assertEquals(200, edited.height)

        val oldSpotPixel = edited.getPixel(140, 140)
        assertEquals(Color.WHITE, oldSpotPixel)

        val newSpotPixel = edited.getPixel(40, 40)
        assertEquals(Color.RED, newSpotPixel)
    }

    @Test
    fun `test stamp rotation and relocation`() {
        val source = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888)
        for (x in 0 until 200) {
            for (y in 0 until 200) {
                source.setPixel(x, y, Color.WHITE)
            }
        }
        for (x in 80 until 120) {
            for (y in 80 until 120) {
                source.setPixel(x, y, Color.BLUE)
            }
        }

        val edited = DocumentImageProcessor.cropAndRelocateStamp(
            sourceBitmap = source,
            cropX = 80,
            cropY = 80,
            cropW = 40,
            cropH = 40,
            newX = 30,
            newY = 30,
            newW = 40,
            newH = 40,
            rotationDegrees = 90f,
            matchPaperBackground = true
        )

        assertNotNull(edited)
        assertEquals(200, edited.width)
        assertEquals(200, edited.height)
        assertEquals(Color.WHITE, edited.getPixel(100, 100))
        assertEquals(Color.BLUE, edited.getPixel(50, 50))
    }

    @Test
    fun `test user registration and password verification`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = AppDatabase.getDatabase(context)
        val userDao = db.userDao()

        val password = "mySecretPassword#2026"
        val passwordHash = SecurityUtils.hashPassword(password)
        val user = User(
            username = "biqilaa_dev",
            email = "biqilaa@oromia.gov.et",
            passwordHash = passwordHash,
            fullName = "Biqilaa Caalaa",
            organization = "Biiroo Barnootaa Oromiyaa",
            roleTitle = "Ogeessa Waraqaa"
        )

        val userId = userDao.registerUser(user)
        assertTrue(userId > 0)

        val retrieved = userDao.findUserByIdentifier("biqilaa_dev")
        assertNotNull(retrieved)
        assertEquals("Biqilaa Caalaa", retrieved?.fullName)

        assertTrue(SecurityUtils.verifyPassword(password, retrieved!!.passwordHash))
        assertFalse(SecurityUtils.verifyPassword("wrongPassword", retrieved.passwordHash))
    }

    @Test
    fun `test text replacement on document`() {
        val source = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888)
        for (x in 0 until 300) {
            for (y in 0 until 200) {
                source.setPixel(x, y, Color.WHITE)
            }
        }
        // Old text area filled with old dark text pixels
        for (x in 50 until 250) {
            for (y in 80 until 120) {
                source.setPixel(x, y, Color.DKGRAY)
            }
        }

        val edited = DocumentImageProcessor.replaceTextOnDocument(
            sourceBitmap = source,
            rectX = 50,
            rectY = 80,
            rectW = 200,
            rectH = 40,
            newText = "Gemedadadi Dadi",
            fontSizePx = 24f,
            fontFamily = "serif",
            isBold = true,
            isItalic = false,
            textColor = Color.BLUE,
            matchPaperBackground = true
        )

        assertNotNull(edited)
        assertEquals(300, edited.width)
        assertEquals(200, edited.height)
        // Corner of the text replacement box should be cleared to white paper color
        val clearedPixel = edited.getPixel(51, 81)
        assertEquals(Color.WHITE, clearedPixel)
    }
}

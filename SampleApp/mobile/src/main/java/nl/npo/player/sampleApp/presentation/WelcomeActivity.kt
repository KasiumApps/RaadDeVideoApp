package nl.npo.player.sampleApp.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import dagger.hilt.android.AndroidEntryPoint
import nl.npo.hackathon.sampleApp.databinding.ActivityWelcomeBinding
import nl.npo.player.sampleApp.shared.extension.observeNonNull
import nl.npo.player.sampleApp.shared.presentation.game.GameState
import nl.npo.player.sampleApp.shared.presentation.game.ProgressState
import nl.npo.player.sampleApp.shared.presentation.viewmodel.GameViewModel
import nl.npo.player.sampleApp.shared.presentation.viewmodel.LibrarySetupViewModel

@AndroidEntryPoint
class WelcomeActivity : BaseActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private val libraryViewModel by viewModels<LibrarySetupViewModel>()
    private val gameModel by viewModels<GameViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkLibraryInitialization()
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.setupViews()
        setObservers()
        logPageAnalytics("WelcomeActivity")
    }

    override fun onStart() {
        super.onStart()
        requestMissingPermissions()
    }

    private fun setObservers() {
        gameModel.gameState.asLiveData().observeNonNull(this, ::handleGameState)
    }

    private fun handleGameState(gameState: GameState) {
        when (gameState.progressState) {
            ProgressState.Init -> {}
            else -> {
                startActivity(Intent(this, GameActivity::class.java))
                finish()
            }
        }
    }

    private fun checkLibraryInitialization() {
        libraryViewModel.setupLibrary(withNPOTag = true)
    }

    private fun ActivityWelcomeBinding.setupViews() {
        btnStart.setOnClickListener {
            val name = etPrid.text.toString()
            if (name.isBlank()) {
                Toast
                    .makeText(this@WelcomeActivity, "Name should not be empty", Toast.LENGTH_LONG)
                    .show()
            }
            gameModel.startGame(name = name)
        }
        etPrid.setOnEditorActionListener { textView, i, _ ->
            return@setOnEditorActionListener if (i == EditorInfo.IME_ACTION_SEND && textView.text.isNotBlank()) {
                btnStart.performClick()
                hideKeyboard()
                true
            } else {
                false
            }
        }
    }

    fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        // Find the currently focused view, so we can grab the correct window token from it.
        var view = currentFocus
        // If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = View(this)
        }
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
        ) { _: Boolean? -> }

    private fun requestMissingPermissions() {
        if (Build.VERSION.SDK_INT < 33) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}

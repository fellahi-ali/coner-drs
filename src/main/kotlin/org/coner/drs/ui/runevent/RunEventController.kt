package org.coner.drs.ui.runevent

import com.github.thomasnield.rxkotlinfx.observeOnFx
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import org.coner.drs.domain.entity.Registration
import org.coner.drs.domain.entity.Run
import org.coner.drs.domain.entity.TimerConfiguration
import org.coner.drs.domain.service.RunService
import org.coner.drs.io.db.entityWatchEventConsumer
import org.coner.drs.io.gateway.RegistrationGateway
import org.coner.drs.io.gateway.RunGateway
import org.coner.drs.io.timer.TimerService
import org.coner.timer.model.FinishTriggerElapsedTimeOnly
import org.coner.timer.output.TimerOutputWriter
import tornadofx.*

class RunEventController : Controller() {
    val model: RunEventModel by inject()
    val registrationGateway: RegistrationGateway by inject()
    val runGateway: RunGateway by inject()
    val runService: RunService by inject()
    val timerService: TimerService by inject()

    fun init() {
        Single.zip(
                registrationGateway.list(model.event),
                runGateway.list(model.event),
                BiFunction { registrations: List<Registration>, runs: List<Run> ->
                    registrations to runs
                }
        ).subscribeOn(Schedulers.io())
                .observeOnFx()
                .subscribe { (registrations, runs) ->
                    model.event.registrations.setAll(registrations)
                    model.event.runs.setAll(runs)
                    runGateway.hydrateWithRegistrationMetadata(runs, registrations)
                }
    }

    fun docked() {
        model.disposables.addAll(
                runGateway.watchList(model.event, model.event.registrations)
                    .subscribeOn(Schedulers.io())
                    .observeOnFx()
                    .subscribe(entityWatchEventConsumer(
                            idProperty = Run::id,
                            list = model.event.runs
                    )),
                registrationGateway.watchList(model.event)
                        .subscribeOn(Schedulers.io())
                        .observeOnFx()
                        .subscribe {
                            model.event.registrations.setAll(it)
                            runGateway.hydrateWithRegistrationMetadata(model.event.runs, it, true)
                        }
        )
    }

    fun undocked() {
        model.disposables.clear()
        timerService.stop()
    }

    fun toggleTimer() {
        if (timerService.model.timer == null) {
            val config = model.timerConfiguration
            when (config) {
                is TimerConfiguration.SerialPortInput -> {
                    timerService.startCommPortTimer(config.port, timerOutputWriter)
                }
                is TimerConfiguration.FileInput -> {
                    timerService.startFileInputTimer(config.file, timerOutputWriter)
                }
            }
        } else {
            timerService.stop()
        }
    }

    private val timerOutputWriter = object : TimerOutputWriter<FinishTriggerElapsedTimeOnly> {
        override fun write(input: FinishTriggerElapsedTimeOnly) {
            runService.addNextTime(model.event, input.et)
        }
    }
}
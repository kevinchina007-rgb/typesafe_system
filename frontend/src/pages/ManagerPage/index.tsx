import { useEffect } from 'react'

import type { LoginManagerType, ManagerPageProps } from './objects'

import { AdvertisementSubmissionWorkspace, HotelProfileSection, ManagerAuthCard, ManagerEntryCard } from './components'
import { managerBusinessEntries, getManagerEntryTitle, getManagerLoginTitle, getManagerRegisterFields, getManagerRegisterTitle, toInitialAirlineSection } from './functions'
import { AttractionManagerPanelSection, SiteAdminPanel, SupplierFeedbackSection, SupplierManagerPanelSection, TrainManagerPanelSection } from './sections'
import { useManagerPageController } from './hooks'

export function ManagerPage(props: ManagerPageProps) {
  const controller = useManagerPageController(props)
  const {
    activeManagerType,
    activeSection,
    selectedEntryType,
    selectedEntryAuthMode,
    currentSupplierManagerSession,
    managedFlightPlannerResponses,
    managedHotelPlannerResponses,
    currentTrainAdminSession,
    currentAttractionAdminSession,
    managerTaskResponses,
    managerRefundTaskResponses,
    advertisementResourceOptions,
    shouldShowWorkspace,
    shouldShowFeedback,
    shouldShowAdvertising,
    shouldShowHotelProfile,
    shouldShowSiteAdminPanel,
    selectEntry,
    clearSelectedEntry,
    ensureUserLoggedOut,
    loginSelectedManager,
    registerSelectedManager,
    reloadManagerTasks,
    reloadManagerRefundTasks,
    reloadManagedFlights,
    loadManagerFlightOrders,
    reloadManagedTrains,
    reloadManagedAttractions,
    logoutManager,
    runAction,
  } = controller
  const { currentLanguage, currentViewKey, translate, onShowNotice, onNavigate, currentManagerSession } = props
  const advertisingSubmitConfig =
    activeManagerType === 'airline'
      ? { placement: 'FlightBookingPage' as const, targetResourceType: 'Flight' as const, resourceView: 'flights' as const }
      : activeManagerType === 'train'
        ? { placement: 'TrainBookingPage' as const, targetResourceType: 'Train' as const, resourceView: 'trains' as const }
        : activeManagerType === 'hotel'
          ? { placement: 'HotelBookingPage' as const, targetResourceType: 'Hotel' as const, resourceView: 'hotels' as const }
          : { placement: 'AttractionBookingPage' as const, targetResourceType: 'Attraction' as const, resourceView: 'attractions' as const }

  useEffect(() => {
    if (currentManagerSession || selectedEntryType) {
      return
    }

    if (currentViewKey === 'siteAdminLogin') {
      selectEntry('siteAdmin', 'login')
    }
  }, [currentManagerSession, currentViewKey, selectedEntryType, selectEntry])

  if (!currentManagerSession) {
    if (selectedEntryType) {
      const entryTitle = getManagerEntryTitle(selectedEntryType, translate)

      return (
        <section className="grid gap-6 px-6 py-8 text-slate-950">
          <ManagerAuthCard
            key={selectedEntryType}
            title={entryTitle}
            registerTitle={getManagerRegisterTitle(selectedEntryType, translate)}
            loginTitle={getManagerLoginTitle(selectedEntryType, translate)}
            initialAuthMode={selectedEntryAuthMode}
            registerFields={getManagerRegisterFields(selectedEntryType, translate)}
            loginManagerType={selectedEntryType}
            isBusy={controller.isBusy}
            onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
            onRegister={async payload => {
              await runAction(async () => {
                await ensureUserLoggedOut()
                await registerSelectedManager(selectedEntryType, payload)
                await loginSelectedManager(selectedEntryType, payload.email.trim(), payload.password)
                clearSelectedEntry()
              }, translate('manager.createAccount'))
            }}
            onLogin={async payload => {
              await runAction(async () => {
                await ensureUserLoggedOut()
                await loginSelectedManager(payload.managerType, payload.email, payload.password)
                clearSelectedEntry()
              }, translate('manager.login'))
            }}
            onBack={clearSelectedEntry}
            hideBack={currentViewKey === 'siteAdminLogin'}
            eyebrow={currentViewKey === 'siteAdminLogin' ? '未归类入口' : undefined}
            allowRegister={currentViewKey === 'siteAdminLogin'}
            translate={translate}
          />
        </section>
      )
    }

    return (
      <section className="grid gap-6 px-6 py-8 text-slate-950">
        <div className="grid gap-2">
          <p className="text-sm font-bold text-slate-500">{translate('nav.managerCenter')}</p>
          <h2 className="m-0 text-3xl font-bold leading-tight text-slate-950">{translate('manager.title')}</h2>
          <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('manager.centerDescription')}</p>
        </div>

        <div className="grid gap-10 md:grid-cols-2 xl:grid-cols-4">
          {managerBusinessEntries.map(entry => (
            <ManagerEntryCard
              key={entry.managerType}
              title={translate(entry.titleKey)}
              shortTitle={entry.shortTitle}
              accentClassName={entry.accentClassName}
              imageSrc={entry.imageSrc}
              imageAlt={translate(entry.titleKey)}
              onSelect={authMode => selectEntry(entry.managerType, authMode)}
            />
          ))}
        </div>
      </section>
    )
  }

  return (
    <>
      <SupplierManagerPanelSection
        isVisible={shouldShowWorkspace && (activeManagerType === 'airline' || activeManagerType === 'hotel')}
        currentLanguage={currentLanguage}
        isBusy={controller.isBusy}
        managerSession={currentSupplierManagerSession}
        managedFlights={managedFlightPlannerResponses}
        managedHotels={managedHotelPlannerResponses}
        managerTasks={managerTaskResponses}
        managerRefundTasks={managerRefundTaskResponses}
        initialAirlineSection={toInitialAirlineSection(currentViewKey)}
        translate={translate}
        onRegisterAirlineManager={async payload => {
          await runAction(async () => {
            await ensureUserLoggedOut()
            await controller.registerAirlineManager(payload)
            await loginSelectedManager('airline', payload.email, payload.password)
          }, translate('manager.createAccount'))
        }}
        onRegisterHotelManager={async payload => {
          await runAction(async () => {
            await ensureUserLoggedOut()
            await controller.registerHotelManager(payload)
            await loginSelectedManager('hotel', payload.email, payload.password)
          }, translate('manager.createAccount'))
        }}
        onCreateManagerRoomType={async payload => {
          await runAction(async () => {
            await controller.createManagerRoomType(payload)
          }, translate('manager.createRoomType'))
        }}
        onLoginManager={async payload => {
          await runAction(async () => {
            await ensureUserLoggedOut()
            await loginSelectedManager(payload.managerType as LoginManagerType, payload.email, payload.password)
          }, translate('manager.login'))
        }}
        onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
        onReloadTasks={async filters => {
          await runAction(async () => {
            await reloadManagerTasks(filters)
          }, translate('manager.refresh'))
        }}
        onReloadRefundTasks={async () => {
          await runAction(async () => {
            await reloadManagerRefundTasks()
          }, translate('manager.refundTasks'))
        }}
        onCreateManagerFlight={async payload => {
          await runAction(async () => {
            await controller.createManagerFlight(payload)
          }, translate('manager.createFlight'))
        }}
        onToggleManagerFlightStatus={async flightId => {
          await runAction(async () => {
            await controller.toggleManagerFlightStatus(flightId)
          }, translate('notice.actionSuccess'))
        }}
        onSearchManagerFlights={async payload => {
          if (!currentManagerSession) {
            return
          }
          await reloadManagedFlights(currentManagerSession.managerId, payload)
        }}
        onLoadManagerFlightOrders={loadManagerFlightOrders}
        onUpdateAirlineManagerProfile={async payload => {
          await runAction(async () => {
            await controller.updateAirlineManagerProfile(payload)
          }, translate('notice.actionSuccess'))
        }}
        onUpdateHotelManagerProfile={async payload => {
          await runAction(async () => {
            await controller.updateHotelManagerProfile(payload)
          }, translate('notice.actionSuccess'))
        }}
        onConfirmTask={async payload => {
          await runAction(async () => {
            await controller.confirmTask(payload)
          }, translate('manager.confirm'))
        }}
        onRejectTask={async payload => {
          await runAction(async () => {
            await controller.rejectTask(payload)
          }, translate('manager.reject'))
        }}
        onBatchConfirmTasks={async payload => {
          await runAction(async () => {
            await controller.batchConfirmTasks(payload)
          }, translate('manager.batchConfirm'))
        }}
        onBatchRejectTasks={async payload => {
          await runAction(async () => {
            await controller.batchRejectTasks(payload)
          }, translate('manager.batchReject'))
        }}
        onApproveRefundTask={async payload => {
          await runAction(async () => {
            await controller.approveRefundTask(payload)
          }, translate('manager.approveRefund'))
        }}
        onRejectRefundTask={async payload => {
          await runAction(async () => {
            await controller.rejectRefundTask(payload)
          }, translate('manager.rejectRefund'))
        }}
        onLogoutManager={() => {
          void runAction(async () => {
            await logoutManager()
          }, translate('manager.logout'))
        }}
      />

      {shouldShowHotelProfile && currentSupplierManagerSession ? (
        <HotelProfileSection
          currentLanguage={currentLanguage}
          managerSession={currentSupplierManagerSession}
          managedHotels={managedHotelPlannerResponses}
          translate={translate}
          onUpdateHotelManagerProfile={async payload => {
            await runAction(async () => {
              await controller.updateHotelManagerProfile(payload)
            }, translate('notice.actionSuccess'))
          }}
          onLogoutManager={() => {
            void runAction(async () => {
              await logoutManager()
            }, translate('manager.logout'))
          }}
        />
      ) : null}

      <TrainManagerPanelSection
        isVisible={shouldShowWorkspace && activeManagerType === 'train'}
        currentLanguage={currentLanguage}
        isBusy={controller.isBusy}
        trainAdminSession={currentTrainAdminSession}
        translate={translate}
        onRegisterRailwayManager={async payload => {
          await runAction(async () => {
            await ensureUserLoggedOut()
            await controller.registerRailwayManager(payload)
          }, translate('trainAdmin.createAccount'))
        }}
        onLoginRailwayManager={async payload => {
          await runAction(async () => {
            await ensureUserLoggedOut()
            await loginSelectedManager('train', payload.email, payload.password)
          }, translate('trainAdmin.login'))
        }}
        onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
        onReloadManagedTrains={async () => {
          if (!currentManagerSession) {
            return
          }
          await runAction(async () => {
            await reloadManagedTrains(currentManagerSession.managerId)
          }, translate('trainAdmin.refresh'))
        }}
        onCreateTrainJourney={async payload => {
          await runAction(async () => {
            await controller.createTrainJourney(payload)
          }, translate('trainAdmin.createTrain'))
        }}
        onLogoutRailwayManager={() => {
          void runAction(async () => {
            await logoutManager()
          }, translate('manager.logout'))
        }}
      />

      <AttractionManagerPanelSection
        isVisible={shouldShowWorkspace && activeManagerType === 'attraction'}
        currentLanguage={currentLanguage}
        isBusy={controller.isBusy}
        attractionAdminSession={currentAttractionAdminSession}
        translate={translate}
        onRegisterAttractionManager={async payload => {
          await runAction(async () => {
            await ensureUserLoggedOut()
            await controller.registerAttractionManager(payload)
          }, translate('attractionAdmin.createAccount'))
        }}
        onLoginAttractionManager={async payload => {
          await runAction(async () => {
            await ensureUserLoggedOut()
            await loginSelectedManager('attraction', payload.email, payload.password)
          }, translate('attractionAdmin.login'))
        }}
        onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
        onReloadManagedAttractions={async () => {
          if (!currentManagerSession) {
            return
          }
          await runAction(async () => {
            await reloadManagedAttractions(currentManagerSession.managerId)
          }, translate('attractionAdmin.refresh'))
        }}
        onCreateAttraction={async payload => {
          await runAction(async () => {
            await controller.createAttraction(payload)
          }, translate('attractionAdmin.createAttraction'))
        }}
        onCreateTicketType={async payload => {
          await runAction(async () => {
            await controller.createAttractionTicketType(payload)
          }, translate('attractionAdmin.createTicketType'))
        }}
        onCreateSession={async payload => {
          await runAction(async () => {
            await controller.createAttractionTicketSession(payload)
          }, translate('attractionAdmin.createSession'))
        }}
        onCreateRule={async payload => {
          await runAction(async () => {
            await controller.createAttractionTicketRule(payload)
          }, translate('attractionAdmin.createRule'))
        }}
        onLogoutAttractionManager={() => {
          void runAction(async () => {
            await logoutManager()
          }, translate('manager.logout'))
        }}
      />

      {shouldShowFeedback ? (
        <SupplierFeedbackSection
          title={translate('manager.feedback.title')}
          currentManagerSession={currentManagerSession}
          managedFlightPlannerResponses={managedFlightPlannerResponses}
          managerTaskResponses={managerTaskResponses}
          managerRefundTaskResponses={managerRefundTaskResponses}
          currentTrainAdminSession={currentTrainAdminSession}
          currentAttractionAdminSession={currentAttractionAdminSession}
          translate={translate}
        />
      ) : null}

      {shouldShowAdvertising ? (
        <AdvertisementSubmissionWorkspace
          defaultPlacement={advertisingSubmitConfig.placement}
          defaultTargetResourceType={advertisingSubmitConfig.targetResourceType}
          resourceOptions={advertisementResourceOptions}
          translate={translate}
          onShowNotice={(kind, title, description) => onShowNotice(kind, title, description)}
          onOpenResource={resourceId => {
            if (advertisingSubmitConfig.resourceView === 'flights') {
              window.sessionStorage.setItem('flight-advertisement-target', resourceId)
            }
            onNavigate(advertisingSubmitConfig.resourceView)
          }}
        />
      ) : null}

      {shouldShowSiteAdminPanel ? (
        <SiteAdminPanel
          currentManagerSession={currentManagerSession}
          section={activeSection === 'advertisingReview' ? 'advertisingReview' : activeSection === 'siteAdminFeedback' ? 'siteAdminFeedback' : 'blogAudit'}
          advertisingModule={
            currentViewKey === 'siteAdminHotelAdvertisingReview'
              ? 'hotel'
              : currentViewKey === 'siteAdminTrainAdvertisingReview'
                ? 'train'
                : currentViewKey === 'siteAdminAttractionAdvertisingReview'
                  ? 'attraction'
                  : 'flight'
          }
          translate={translate}
        />
      ) : null}
    </>
  )
}

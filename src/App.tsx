import React, { useState, useEffect } from 'react';
import {
  HostUser,
  HostRole,
  OmbMatch,
  Tournament,
  OperationalAlert,
  HostNotification,
  HostTab
} from './types';
import { HostStorageService } from './services/hostStorage';
import { Header } from './components/Header';
import { LoginView } from './components/LoginView';
import { OmbAvailableView } from './components/OmbAvailableView';
import { OmbRunningView } from './components/OmbRunningView';
import { TournamentAvailableView } from './components/TournamentAvailableView';
import { TournamentRunningView } from './components/TournamentRunningView';
import { NotificationDrawer } from './components/NotificationDrawer';
import { OperationalAlertBanner } from './components/OperationalAlertBanner';
import { CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react';

export const App: React.FC = () => {
  const [currentHost, setCurrentHost] = useState<HostUser | null>(() =>
    HostStorageService.getStoredHost()
  );
  const [availableOmbs, setAvailableOmbs] = useState<OmbMatch[]>([]);
  const [runningOmb, setRunningOmb] = useState<OmbMatch | null>(null);
  const [availableTournaments, setAvailableTournaments] = useState<Tournament[]>([]);
  const [runningTournament, setRunningTournament] = useState<Tournament | null>(null);
  const [operationalAlerts, setOperationalAlerts] = useState<OperationalAlert[]>([]);
  const [notifications, setNotifications] = useState<HostNotification[]>([]);
  const [selectedTab, setSelectedTab] = useState<HostTab>('AVAILABLE');
  const [showNotificationDrawer, setShowNotificationDrawer] = useState(false);
  const [toast, setToast] = useState<{ type: 'success' | 'error'; message: string } | null>(null);
  const [authError, setAuthError] = useState<string | null>(null);

  // Load data
  const loadData = () => {
    const ombs = HostStorageService.getOmbs();
    const tourneys = HostStorageService.getTournaments();
    const alerts = HostStorageService.getAlerts();
    const notifs = HostStorageService.getNotifications();
    const host = HostStorageService.getStoredHost();

    setAvailableOmbs(ombs.filter((m) => m.status === 'AVAILABLE'));
    setAvailableTournaments(tourneys.filter((t) => t.status === 'AVAILABLE'));
    setOperationalAlerts(alerts);
    setNotifications(notifs);

    if (host?.currentAssignmentId) {
      if (host.currentAssignmentType === 'OMB' || host.role === 'OMB_HOST') {
        const running = ombs.find((m) => m.id === host.currentAssignmentId);
        setRunningOmb(running || null);
      }
      if (host.currentAssignmentType === 'TOURNAMENT' || host.role === 'TOURNAMENT_HOST') {
        const runningT = tourneys.find((t) => t.id === host.currentAssignmentId);
        setRunningTournament(runningT || null);
      }
    } else {
      setRunningOmb(null);
      setRunningTournament(null);
    }
  };

  useEffect(() => {
    loadData();
  }, [currentHost?.id, currentHost?.currentAssignmentId]);

  const showToast = (message: string, type: 'success' | 'error' = 'success') => {
    setToast({ message, type });
    setTimeout(() => {
      setToast(null);
    }, 4000);
  };

  // Auth
  const handleLogin = (hostId: string, _password: string, role: HostRole) => {
    const demoHosts = HostStorageService.getDemoHosts();
    const matched = demoHosts.find(
      (h) => h.id.toLowerCase() === hostId.toLowerCase()
    );

    if (!matched) {
      setAuthError(`Invalid Host ID (${hostId}). Try HOST_OMB_01 or HOST_TOURN_01.`);
      return;
    }

    const hostWithRole: HostUser = {
      ...matched,
      role
    };

    HostStorageService.saveStoredHost(hostWithRole);
    setCurrentHost(hostWithRole);
    setAuthError(null);
    setSelectedTab(hostWithRole.currentAssignmentId ? 'RUNNING' : 'AVAILABLE');
    showToast(`Welcome, ${hostWithRole.fullName}! Signed in as ${role}.`);
  };

  const handleLogout = () => {
    HostStorageService.saveStoredHost(null);
    setCurrentHost(null);
    setRunningOmb(null);
    setRunningTournament(null);
    showToast('Logged out successfully.');
  };

  const handleResetData = () => {
    HostStorageService.resetToDefault();
    const host = currentHost
      ? { ...currentHost, currentAssignmentId: null, currentAssignmentType: null }
      : null;
    HostStorageService.saveStoredHost(host);
    setCurrentHost(host);
    loadData();
    showToast('Database reset to fresh seeds.');
  };

  // OMB Actions
  const handleClaimOmb = (matchId: string) => {
    if (!currentHost) return;
    if (currentHost.status === 'DISABLED') {
      showToast('Action Denied: Your host account is disabled by Administrator.', 'error');
      return;
    }
    if (currentHost.currentAssignmentId) {
      showToast(
        `Active Assignment Rule: Complete or release assignment #${currentHost.currentAssignmentId} first.`,
        'error'
      );
      return;
    }

    const ombs = HostStorageService.getOmbs();
    const matchIndex = ombs.findIndex((m) => m.id === matchId);
    if (matchIndex === -1) return;

    const updatedMatch: OmbMatch = {
      ...ombs[matchIndex],
      status: 'RUNNING',
      claimedByHostId: currentHost.id
    };

    ombs[matchIndex] = updatedMatch;
    HostStorageService.saveOmbs(ombs);

    const updatedHost: HostUser = {
      ...currentHost,
      currentAssignmentId: matchId,
      currentAssignmentType: 'OMB'
    };
    HostStorageService.saveStoredHost(updatedHost);
    setCurrentHost(updatedHost);

    setRunningOmb(updatedMatch);
    setAvailableOmbs(ombs.filter((m) => m.status === 'AVAILABLE'));
    setSelectedTab('RUNNING');
    showToast(`Claimed match #${matchId}. Proceed with Room credentials double verification.`);
  };

  const handleUpdateRoomCredentials = (matchId: string, roomId: string, password: string) => {
    const ombs = HostStorageService.getOmbs();
    const idx = ombs.findIndex((m) => m.id === matchId);
    if (idx === -1) return;

    ombs[idx] = {
      ...ombs[idx],
      roomId,
      roomPassword: password
    };
    HostStorageService.saveOmbs(ombs);
    setRunningOmb(ombs[idx]);
    showToast('Room ID & Password double-verified and updated successfully.');
  };

  const handleSaveParticipantKills = (matchId: string, participantId: string, kills: number) => {
    const ombs = HostStorageService.getOmbs();
    const idx = ombs.findIndex((m) => m.id === matchId);
    if (idx === -1) return;

    const participants = ombs[idx].participants.map((p) =>
      p.id === participantId ? { ...p, kills } : p
    );

    ombs[idx] = {
      ...ombs[idx],
      participants
    };
    HostStorageService.saveOmbs(ombs);
    setRunningOmb(ombs[idx]);
    showToast('Participant kills recorded with double verification.');
  };

  const handleUploadScreenshot = (matchId: string, url: string) => {
    const ombs = HostStorageService.getOmbs();
    const idx = ombs.findIndex((m) => m.id === matchId);
    if (idx === -1) return;

    ombs[idx] = {
      ...ombs[idx],
      screenshotUrl: url
    };
    HostStorageService.saveOmbs(ombs);
    setRunningOmb(ombs[idx]);
    showToast('Overall match result scoreboard attached.');
  };

  const handleTagHackerOmb = (matchId: string, participantId: string, reason: string) => {
    const ombs = HostStorageService.getOmbs();
    const idx = ombs.findIndex((m) => m.id === matchId);
    if (idx === -1) return;

    const participants = ombs[idx].participants.map((p) =>
      p.id === participantId
        ? {
            ...p,
            isHackerCheater: true,
            hackerReason: reason,
            kills: 0
          }
        : p
    );

    ombs[idx] = {
      ...ombs[idx],
      participants
    };
    HostStorageService.saveOmbs(ombs);
    setRunningOmb(ombs[idx]);

    // Add alert
    const alerts = HostStorageService.getAlerts();
    const newAlert: OperationalAlert = {
      id: `alert_${Date.now()}`,
      severity: 'CRITICAL',
      message: `Participant tagged as Hacker in match #${matchId}: Prize and refund forfeited.`,
      timestamp: Date.now(),
      matchOrTournamentId: matchId,
      matchDetails: 'Cheating Policy Enforcement'
    };
    alerts.unshift(newAlert);
    HostStorageService.saveAlerts(alerts);
    setOperationalAlerts([...alerts]);

    showToast('Participant permanently tagged as Hacker (Prize = ₹0, Refund = ₹0).', 'error');
  };

  const handleReleaseOmb = (matchId: string, _reason: string) => {
    const ombs = HostStorageService.getOmbs();
    const idx = ombs.findIndex((m) => m.id === matchId);
    if (idx === -1) return;

    ombs[idx] = {
      ...ombs[idx],
      status: 'AVAILABLE',
      claimedByHostId: null,
      roomId: null,
      roomPassword: null
    };
    HostStorageService.saveOmbs(ombs);

    if (currentHost) {
      const updatedHost: HostUser = {
        ...currentHost,
        currentAssignmentId: null,
        currentAssignmentType: null
      };
      HostStorageService.saveStoredHost(updatedHost);
      setCurrentHost(updatedHost);
    }

    setRunningOmb(null);
    setAvailableOmbs(ombs.filter((m) => m.status === 'AVAILABLE'));
    setSelectedTab('AVAILABLE');
    showToast(`Assignment #${matchId} released back to Available pool.`);
  };

  const handleCompleteOmb = (matchId: string) => {
    const ombs = HostStorageService.getOmbs();
    const idx = ombs.findIndex((m) => m.id === matchId);
    if (idx === -1) return;

    const match = ombs[idx];
    if (!match.screenshotUrl) {
      showToast('Error: Match result scoreboard screenshot is mandatory.', 'error');
      return;
    }

    ombs[idx] = {
      ...match,
      status: 'COMPLETED'
    };
    HostStorageService.saveOmbs(ombs);
    setRunningOmb(ombs[idx]);

    if (currentHost) {
      const updatedHost: HostUser = {
        ...currentHost,
        currentAssignmentId: null,
        currentAssignmentType: null
      };
      HostStorageService.saveStoredHost(updatedHost);
      setCurrentHost(updatedHost);
    }

    showToast(`Match #${matchId} completed! Payouts calculated and finalized.`);
  };

  // Tournament Actions
  const handleClaimTournament = (tourneyId: string) => {
    if (!currentHost) return;
    if (currentHost.status === 'DISABLED') {
      showToast('Action Denied: Your host account is disabled by Administrator.', 'error');
      return;
    }
    if (currentHost.currentAssignmentId) {
      showToast(
        `Active Assignment Rule: Complete or release assignment #${currentHost.currentAssignmentId} first.`,
        'error'
      );
      return;
    }

    const tourneys = HostStorageService.getTournaments();
    const idx = tourneys.findIndex((t) => t.id === tourneyId);
    if (idx === -1) return;

    const updatedTourney: Tournament = {
      ...tourneys[idx],
      status: 'RUNNING',
      claimedByHostId: currentHost.id
    };

    tourneys[idx] = updatedTourney;
    HostStorageService.saveTournaments(tourneys);

    const updatedHost: HostUser = {
      ...currentHost,
      currentAssignmentId: tourneyId,
      currentAssignmentType: 'TOURNAMENT'
    };
    HostStorageService.saveStoredHost(updatedHost);
    setCurrentHost(updatedHost);

    setRunningTournament(updatedTourney);
    setAvailableTournaments(tourneys.filter((t) => t.status === 'AVAILABLE'));
    setSelectedTab('RUNNING');
    showToast(`Claimed tournament #${tourneyId}. Begin double verification of participant metrics.`);
  };

  const handleSaveTournamentInitialValue = (tourneyId: string, participantId: string, value: number) => {
    const tourneys = HostStorageService.getTournaments();
    const idx = tourneys.findIndex((t) => t.id === tourneyId);
    if (idx === -1) return;

    const participants = tourneys[idx].participants.map((p) => {
      if (p.id !== participantId) return p;
      const perf = p.finalValue !== null && p.finalValue !== undefined ? p.finalValue - value : null;
      return {
        ...p,
        initialValue: value,
        performance: perf
      };
    });

    tourneys[idx] = {
      ...tourneys[idx],
      participants
    };
    HostStorageService.saveTournaments(tourneys);
    setRunningTournament(tourneys[idx]);
    showToast('Initial metric value recorded with double verification.');
  };

  const handleSaveTournamentFinalValue = (tourneyId: string, participantId: string, value: number) => {
    const tourneys = HostStorageService.getTournaments();
    const idx = tourneys.findIndex((t) => t.id === tourneyId);
    if (idx === -1) return;

    const participants = tourneys[idx].participants.map((p) => {
      if (p.id !== participantId) return p;
      const initial = p.initialValue || 0;
      return {
        ...p,
        finalValue: value,
        performance: value - initial
      };
    });

    // Auto rank participants by performance
    const sorted = [...participants]
      .filter((p) => !p.isHackerCheater && p.performance !== null && p.performance !== undefined)
      .sort((a, b) => (b.performance ?? 0) - (a.performance ?? 0));

    const rankedParticipants = participants.map((p) => {
      if (p.isHackerCheater) return { ...p, rank: null };
      const rankIdx = sorted.findIndex((s) => s.id === p.id);
      return {
        ...p,
        rank: rankIdx >= 0 ? rankIdx + 1 : null
      };
    });

    tourneys[idx] = {
      ...tourneys[idx],
      participants: rankedParticipants
    };
    HostStorageService.saveTournaments(tourneys);
    setRunningTournament(tourneys[idx]);
    showToast('Final metric value recorded. Standings and rankings updated.');
  };

  const handleSubmitTournamentSchedule = (
    tourneyId: string,
    scheduleId: string,
    valuesMap: Record<string, number>
  ) => {
    const tourneys = HostStorageService.getTournaments();
    const idx = tourneys.findIndex((t) => t.id === tourneyId);
    if (idx === -1) return;

    const tourney = tourneys[idx];
    const nowTime = new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    // Compute standings sorted desc
    const sortedEntries = Object.entries(valuesMap).sort(([, a], [, b]) => b - a);

    const standings = sortedEntries.map(([participantId, metricVal], posIdx) => {
      const p = tourney.participants.find((item) => item.id === participantId);
      return {
        position: posIdx + 1,
        gameId: p?.gameIdName || participantId,
        metricValue: metricVal,
        atTime: nowTime
      };
    });

    const schedules = tourney.schedules.map((s) =>
      s.id === scheduleId ? { ...s, isSubmitted: true, standings } : s
    );

    tourneys[idx] = {
      ...tourney,
      schedules
    };
    HostStorageService.saveTournaments(tourneys);
    setRunningTournament(tourneys[idx]);
    showToast('Competitor standings chart computed and published.');
  };

  const handleTagHackerTournament = (tourneyId: string, participantId: string, reason: string) => {
    const tourneys = HostStorageService.getTournaments();
    const idx = tourneys.findIndex((t) => t.id === tourneyId);
    if (idx === -1) return;

    const participants = tourneys[idx].participants.map((p) =>
      p.id === participantId
        ? {
            ...p,
            isHackerCheater: true,
            hackerReason: reason,
            rank: null,
            performance: null
          }
        : p
    );

    tourneys[idx] = {
      ...tourneys[idx],
      participants
    };
    HostStorageService.saveTournaments(tourneys);
    setRunningTournament(tourneys[idx]);

    // Add alert
    const alerts = HostStorageService.getAlerts();
    const newAlert: OperationalAlert = {
      id: `alert_${Date.now()}`,
      severity: 'CRITICAL',
      message: `Player flagged as Hacker in Tournament #${tourneyId}: Disqualified with zero payout.`,
      timestamp: Date.now(),
      matchOrTournamentId: tourneyId,
      matchDetails: 'Tournament Integrity'
    };
    alerts.unshift(newAlert);
    HostStorageService.saveAlerts(alerts);
    setOperationalAlerts([...alerts]);

    showToast('Player tagged as Hacker and disqualified from tournament rankings.', 'error');
  };

  const handleReleaseTournament = (tourneyId: string, _reason: string) => {
    const tourneys = HostStorageService.getTournaments();
    const idx = tourneys.findIndex((t) => t.id === tourneyId);
    if (idx === -1) return;

    tourneys[idx] = {
      ...tourneys[idx],
      status: 'AVAILABLE',
      claimedByHostId: null
    };
    HostStorageService.saveTournaments(tourneys);

    if (currentHost) {
      const updatedHost: HostUser = {
        ...currentHost,
        currentAssignmentId: null,
        currentAssignmentType: null
      };
      HostStorageService.saveStoredHost(updatedHost);
      setCurrentHost(updatedHost);
    }

    setRunningTournament(null);
    setAvailableTournaments(tourneys.filter((t) => t.status === 'AVAILABLE'));
    setSelectedTab('AVAILABLE');
    showToast(`Tournament #${tourneyId} released back to Available pool.`);
  };

  const handleClaimFromNotification = (matchId: string, type: 'OMB' | 'TOURNAMENT') => {
    if (type === 'OMB') {
      handleClaimOmb(matchId);
    } else {
      handleClaimTournament(matchId);
    }
  };

  if (!currentHost) {
    return <LoginView onLogin={handleLogin} error={authError} />;
  }

  const isOmbHost = currentHost.role === 'OMB_HOST';
  const availableCount = isOmbHost ? availableOmbs.length : availableTournaments.length;
  const hasRunningAssignment = isOmbHost ? !!runningOmb : !!runningTournament;

  return (
    <div className="min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans selection:bg-indigo-500 selection:text-white">
      {/* Toast Notification */}
      {toast && (
        <div className="fixed bottom-6 right-6 z-50 animate-in fade-in slide-in-from-bottom-5 duration-300 max-w-sm">
          <div
            className={`p-4 rounded-2xl border shadow-2xl flex items-center gap-3 ${
              toast.type === 'success'
                ? 'bg-emerald-950/90 border-emerald-700/60 text-emerald-200'
                : 'bg-red-950/90 border-red-700/60 text-red-200'
            }`}
          >
            {toast.type === 'success' ? (
              <CheckCircle2 className="w-5 h-5 text-emerald-400 shrink-0" />
            ) : (
              <AlertCircle className="w-5 h-5 text-red-400 shrink-0" />
            )}
            <span className="text-xs font-semibold">{toast.message}</span>
          </div>
        </div>
      )}

      {/* Header */}
      <Header
        host={currentHost}
        unreadNotificationsCount={notifications.length}
        onOpenNotifications={() => setShowNotificationDrawer(true)}
        onLogout={handleLogout}
        onResetData={handleResetData}
      />

      {/* Operational Alert Banner */}
      <OperationalAlertBanner alerts={operationalAlerts} />

      {/* Navigation Tabs */}
      <div className="bg-slate-900/60 border-b border-slate-800/80 px-4">
        <div className="max-w-7xl mx-auto flex items-center justify-between">
          <div className="flex space-x-1">
            <button
              type="button"
              onClick={() => setSelectedTab('AVAILABLE')}
              className={`py-3.5 px-5 text-xs font-bold transition flex items-center gap-2 border-b-2 cursor-pointer ${
                selectedTab === 'AVAILABLE'
                  ? isOmbHost
                    ? 'border-indigo-500 text-indigo-400'
                    : 'border-emerald-500 text-emerald-400'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              <span>Available</span>
              {availableCount > 0 && (
                <span
                  className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                    isOmbHost
                      ? 'bg-indigo-500/10 text-indigo-300 border border-indigo-500/20'
                      : 'bg-emerald-500/10 text-emerald-300 border border-emerald-500/20'
                  }`}
                >
                  {availableCount}
                </span>
              )}
            </button>

            <button
              type="button"
              onClick={() => setSelectedTab('RUNNING')}
              className={`py-3.5 px-5 text-xs font-bold transition flex items-center gap-2 border-b-2 cursor-pointer ${
                selectedTab === 'RUNNING'
                  ? isOmbHost
                    ? 'border-indigo-500 text-indigo-400'
                    : 'border-emerald-500 text-emerald-400'
                  : 'border-transparent text-slate-400 hover:text-slate-200'
              }`}
            >
              <span>Running</span>
              {hasRunningAssignment && (
                <span className="w-2 h-2 rounded-full bg-emerald-400 animate-pulse" />
              )}
            </button>
          </div>

          <div className="text-[11px] text-slate-500 hidden sm:flex items-center gap-1.5">
            <span>Assignment:</span>
            <strong className="text-slate-300 font-mono">
              {currentHost.currentAssignmentId ? `#${currentHost.currentAssignmentId}` : 'None (Idle)'}
            </strong>
          </div>
        </div>
      </div>

      {/* Main Content Area */}
      <main className="flex-1 pb-16">
        {isOmbHost ? (
          selectedTab === 'AVAILABLE' ? (
            <OmbAvailableView
              availableOmbs={availableOmbs}
              currentHost={currentHost}
              onClaimOmb={handleClaimOmb}
              onRefresh={loadData}
            />
          ) : (
            <OmbRunningView
              match={runningOmb}
              onUpdateRoomCredentials={handleUpdateRoomCredentials}
              onSaveParticipantKills={handleSaveParticipantKills}
              onUploadScreenshot={handleUploadScreenshot}
              onTagHacker={handleTagHackerOmb}
              onReleaseMatch={handleReleaseOmb}
              onCompleteMatch={handleCompleteOmb}
            />
          )
        ) : (
          selectedTab === 'AVAILABLE' ? (
            <TournamentAvailableView
              availableTournaments={availableTournaments}
              currentHost={currentHost}
              onClaimTournament={handleClaimTournament}
              onRefresh={loadData}
            />
          ) : (
            <TournamentRunningView
              tournament={runningTournament}
              onSaveInitialValue={handleSaveTournamentInitialValue}
              onSaveFinalValue={handleSaveTournamentFinalValue}
              onSubmitSchedule={handleSubmitTournamentSchedule}
              onTagHacker={handleTagHackerTournament}
              onReleaseTournament={handleReleaseTournament}
            />
          )
        )}
      </main>

      {/* Slide-over Notifications */}
      {showNotificationDrawer && (
        <NotificationDrawer
          notifications={notifications}
          onClaim={handleClaimFromNotification}
          onDismiss={() => setShowNotificationDrawer(false)}
        />
      )}
    </div>
  );
};

export default App;

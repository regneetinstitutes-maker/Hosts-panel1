import React, { useState } from 'react';
import {
  Trophy,
  LockOpen,
  Calendar,
  Users,
  Coins,
  Clock,
  Award,
  AlertTriangle,
  Skull,
  TrendingUp,
  CheckCircle2,
  Edit3
} from 'lucide-react';
import { Tournament, TournamentParticipant, TournamentSchedule } from '../types';
import { DoubleVerificationModal } from './DoubleVerificationModal';
import { HackerTagModal } from './HackerTagModal';
import { ScheduleValuesModal } from './ScheduleValuesModal';
import { ReleaseModal } from './ReleaseModal';

interface TournamentRunningViewProps {
  tournament: Tournament | null;
  onSaveInitialValue: (tourneyId: string, participantId: string, value: number) => void;
  onSaveFinalValue: (tourneyId: string, participantId: string, value: number) => void;
  onSubmitSchedule: (tourneyId: string, scheduleId: string, values: Record<string, number>) => void;
  onTagHacker: (tourneyId: string, participantId: string, reason: string) => void;
  onReleaseTournament: (tourneyId: string, reason: string) => void;
}

export const TournamentRunningView: React.FC<TournamentRunningViewProps> = ({
  tournament,
  onSaveInitialValue,
  onSaveFinalValue,
  onSubmitSchedule,
  onTagHacker,
  onReleaseTournament
}) => {
  const [participantForInitialVal, setParticipantForInitialVal] = useState<TournamentParticipant | null>(null);
  const [participantForFinalVal, setParticipantForFinalVal] = useState<TournamentParticipant | null>(null);
  const [participantForHacker, setParticipantForHacker] = useState<TournamentParticipant | null>(null);
  const [scheduleToFill, setScheduleToFill] = useState<TournamentSchedule | null>(null);
  const [showReleaseModal, setShowReleaseModal] = useState(false);

  if (!tournament) {
    return (
      <div className="text-center py-24 bg-slate-900/40 border border-slate-800/60 rounded-3xl max-w-4xl mx-auto my-12 px-6 space-y-4">
        <div className="w-14 h-14 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center mx-auto text-emerald-400">
          <Trophy className="w-7 h-7" />
        </div>
        <h3 className="text-lg font-bold text-white">No Running Tournament Assignment</h3>
        <p className="text-xs text-slate-400 max-w-md mx-auto">
          You do not currently have a claimed tournament assignment. Switch to the <strong>Available</strong> tab to claim a tournament.
        </p>
      </div>
    );
  }

  const isCompleted = tournament.status === 'COMPLETED';
  const isCancelled = tournament.status === 'CANCELLED';
  const isInteractive = !isCompleted && !isCancelled;

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 py-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 bg-slate-900/90 border border-slate-800 rounded-3xl shadow-xl">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
            <Trophy className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2.5">
              <h2 className="text-lg font-black text-white">Running Tournament</h2>
              <span
                className={`text-[10px] font-bold px-2.5 py-0.5 rounded-full ${
                  tournament.status === 'RUNNING'
                    ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                    : tournament.status === 'COMPLETED'
                    ? 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
                    : 'bg-red-500/20 text-red-300 border border-red-500/30'
                }`}
              >
                {tournament.status}
              </span>
            </div>
            <div className="text-xs text-slate-400 mt-0.5">
              Tournament ID: <strong className="text-emerald-400 font-mono">#{tournament.id}</strong> • {tournament.game} ({tournament.mode})
            </div>
          </div>
        </div>

        {isInteractive && (
          <div className="flex items-center gap-2">
            <button
              onClick={() => setShowReleaseModal(true)}
              className="px-3.5 py-2 text-xs font-bold text-red-400 border border-red-900/60 bg-red-950/30 hover:bg-red-900/40 rounded-xl transition flex items-center gap-1.5 cursor-pointer"
            >
              <LockOpen className="w-3.5 h-3.5" />
              Release Assignment
            </button>
          </div>
        )}
      </div>

      {/* Admin Configured Details */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
        <div className="text-xs font-bold text-emerald-400 uppercase tracking-wider">
          Admin Configured Tournament Settings
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <div className="p-3.5 rounded-2xl bg-slate-950/60 border border-slate-800/60">
            <div className="text-[10px] text-slate-400 uppercase font-semibold">Tracked Metric</div>
            <div className="text-sm font-bold text-emerald-400 mt-1">{tournament.tournamentMetric}</div>
          </div>
          <div className="p-3.5 rounded-2xl bg-slate-950/60 border border-slate-800/60">
            <div className="text-[10px] text-slate-400 uppercase font-semibold">Entry Fee</div>
            <div className="text-sm font-bold text-white mt-1">₹{tournament.entryFee}</div>
          </div>
          <div className="p-3.5 rounded-2xl bg-slate-950/60 border border-slate-800/60">
            <div className="text-[10px] text-slate-400 uppercase font-semibold">Duration</div>
            <div className="text-sm font-bold text-white mt-1">{tournament.durationMinutes} mins</div>
          </div>
          <div className="p-3.5 rounded-2xl bg-slate-950/60 border border-slate-800/60">
            <div className="text-[10px] text-slate-400 uppercase font-semibold">Results On</div>
            <div className="text-sm font-bold text-slate-300 mt-1 truncate">{tournament.resultsOn}</div>
          </div>
        </div>

        <div className="p-3 rounded-2xl bg-slate-950/40 border border-slate-800/60 flex items-center justify-between text-xs text-slate-300">
          <span className="text-slate-400 font-medium">Prize Chart Distribution:</span>
          <span className="text-amber-300 font-semibold">
            {tournament.prizeChart.map((p) => `Rank #${p.rank}: ₹${p.prize}`).join('  |  ')}
          </span>
        </div>
      </div>

      {/* Participants & Metric Tracking Section */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
        <div className="flex items-center justify-between">
          <div>
            <h3 className="text-base font-bold text-white flex items-center gap-2">
              <Users className="w-4 h-4 text-emerald-400" />
              Participants & Double-Verified Metric Tracking
            </h3>
            <p className="text-xs text-slate-400">
              Host must record both initial baseline and final values with double verification.
            </p>
          </div>
          <span className="text-xs font-bold text-emerald-400 bg-emerald-500/10 px-3 py-1.5 rounded-xl border border-emerald-500/20">
            Metric: {tournament.tournamentMetric}
          </span>
        </div>

        <div className="space-y-3">
          {tournament.participants.map((p) => {
            const isHacker = p.isHackerCheater;
            return (
              <div
                key={p.id}
                className={`p-4 rounded-2xl border transition flex flex-col md:flex-row md:items-center justify-between gap-4 ${
                  isHacker
                    ? 'bg-red-950/20 border-red-900/40'
                    : 'bg-slate-950/60 border-slate-800 hover:border-slate-700/80'
                }`}
              >
                <div>
                  <div className="flex items-center gap-2">
                    <span className="text-sm font-bold text-white">{p.name}</span>
                    {p.rank && (
                      <span className="text-[10px] font-bold px-2 py-0.5 rounded-md bg-emerald-500/20 text-emerald-300 border border-emerald-500/30">
                        Rank #{p.rank}
                      </span>
                    )}
                    {isHacker && (
                      <span className="text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-md bg-red-600 text-white">
                        HACKER
                      </span>
                    )}
                  </div>
                  <div className="text-xs text-slate-400 mt-1 flex items-center gap-2">
                    <span>Game ID: <strong className="text-slate-300 font-mono">{p.gameIdName}</strong></span>
                    <span>•</span>
                    <span>UID: <span className="font-mono text-slate-400">{p.inGameUid}</span></span>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  {/* Initial Value Box */}
                  <div
                    onClick={() => {
                      if (isInteractive && !isHacker && p.initialValue === null) {
                        setParticipantForInitialVal(p);
                      }
                    }}
                    className={`p-2.5 rounded-xl border text-center min-w-[110px] transition ${
                      p.initialValue !== null
                        ? 'bg-slate-900 border-emerald-500/40 text-emerald-400'
                        : isInteractive && !isHacker
                        ? 'bg-indigo-950/30 border-indigo-500/40 text-indigo-400 hover:bg-indigo-900/40 cursor-pointer'
                        : 'bg-slate-900/40 border-slate-800 text-slate-500'
                    }`}
                  >
                    <div className="text-[10px] text-slate-400 font-medium">Initial</div>
                    <div className="text-xs font-bold mt-0.5">
                      {p.initialValue !== null ? `${p.initialValue} ✅` : '+ Enter Value'}
                    </div>
                  </div>

                  {/* Final Value Box */}
                  <div
                    onClick={() => {
                      if (isInteractive && !isHacker && p.initialValue !== null && p.finalValue === null) {
                        setParticipantForFinalVal(p);
                      }
                    }}
                    className={`p-2.5 rounded-xl border text-center min-w-[110px] transition ${
                      p.finalValue !== null
                        ? 'bg-slate-900 border-emerald-500/40 text-emerald-400'
                        : p.initialValue !== null && isInteractive && !isHacker
                        ? 'bg-emerald-950/30 border-emerald-500/40 text-emerald-400 hover:bg-emerald-900/40 cursor-pointer'
                        : 'bg-slate-900/40 border-slate-800 text-slate-500'
                    }`}
                  >
                    <div className="text-[10px] text-slate-400 font-medium">Final</div>
                    <div className="text-xs font-bold mt-0.5">
                      {p.finalValue !== null
                        ? `${p.finalValue} ✅`
                        : p.initialValue !== null
                        ? '+ Enter Final'
                        : 'Pending Initial'}
                    </div>
                  </div>

                  {/* Performance Delta Box */}
                  <div className="p-2.5 rounded-xl bg-slate-900/80 border border-slate-800 text-center min-w-[100px]">
                    <div className="text-[10px] text-slate-400 font-medium">Performance</div>
                    <div className="text-xs font-black text-indigo-300 mt-0.5">
                      {p.performance !== null ? `+${p.performance}` : '—'}
                    </div>
                  </div>

                  {/* Hacker Tag Button */}
                  {!isHacker && isInteractive && (
                    <button
                      type="button"
                      onClick={() => setParticipantForHacker(p)}
                      title="Tag as Hacker / Cheater"
                      className="p-2 text-slate-500 hover:text-red-400 hover:bg-red-950/30 rounded-xl transition cursor-pointer"
                    >
                      <Skull className="w-4 h-4" />
                    </button>
                  )}
                </div>
              </div>
            );
          })}
        </div>
      </div>

      {/* Competitors Position Schedules */}
      <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
        <div className="flex items-center gap-2">
          <Calendar className="w-5 h-5 text-emerald-400" />
          <div>
            <h3 className="text-base font-bold text-white">Competitor Position Schedules</h3>
            <p className="text-xs text-slate-400">
              Admin configured interim reveal charts for audience and player leaderboards.
            </p>
          </div>
        </div>

        <div className="space-y-3">
          {tournament.schedules.map((schedule) => (
            <div
              key={schedule.id}
              className={`p-4 rounded-2xl border transition ${
                schedule.isSubmitted
                  ? 'bg-emerald-950/20 border-emerald-800/40'
                  : 'bg-slate-950/60 border-slate-800'
              }`}
            >
              <div className="flex items-center justify-between">
                <div>
                  <h4 className="text-sm font-bold text-white">{schedule.name}</h4>
                  <span className="text-xs text-emerald-400 font-semibold">Scheduled: {schedule.time}</span>
                </div>

                {!schedule.isSubmitted && isInteractive && (
                  <button
                    type="button"
                    onClick={() => setScheduleToFill(schedule)}
                    className="px-3.5 py-1.5 rounded-xl bg-emerald-600 hover:bg-emerald-500 text-white text-xs font-bold transition flex items-center gap-1.5 cursor-pointer shadow-sm"
                  >
                    <Edit3 className="w-3.5 h-3.5" />
                    Enter Values for {schedule.name}
                  </button>
                )}
              </div>

              {schedule.isSubmitted && schedule.standings.length > 0 && (
                <div className="mt-3 pt-3 border-t border-slate-800/60 space-y-2">
                  <div className="text-xs font-bold text-emerald-400 flex items-center gap-1.5">
                    <CheckCircle2 className="w-3.5 h-3.5" /> Published Standings Leaderboard:
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-4 gap-2">
                    {schedule.standings.map((st) => (
                      <div
                        key={st.gameId}
                        className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 flex items-center justify-between text-xs"
                      >
                        <div>
                          <span className="font-bold text-emerald-300">Pos #{st.position}</span>
                          <div className="text-[11px] text-slate-300 font-mono">{st.gameId}</div>
                        </div>
                        <div className="text-right">
                          <span className="font-bold text-white">{st.metricValue} pts</span>
                          <div className="text-[10px] text-slate-500">{st.atTime}</div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {/* Dialogs */}
      {participantForInitialVal && (
        <DoubleVerificationModal
          title={`Enter Initial ${tournament.tournamentMetric}: ${participantForInitialVal.name}`}
          primaryFieldLabel={`Initial ${tournament.tournamentMetric}`}
          isNumeric={true}
          onDismiss={() => setParticipantForInitialVal(null)}
          onConfirmed={(valStr) => {
            const val = parseInt(valStr, 10) || 0;
            onSaveInitialValue(tournament.id, participantForInitialVal.id, val);
            setParticipantForInitialVal(null);
          }}
        />
      )}

      {participantForFinalVal && (
        <DoubleVerificationModal
          title={`Enter Final ${tournament.tournamentMetric}: ${participantForFinalVal.name}`}
          primaryFieldLabel={`Final ${tournament.tournamentMetric}`}
          isNumeric={true}
          onDismiss={() => setParticipantForFinalVal(null)}
          onConfirmed={(valStr) => {
            const val = parseInt(valStr, 10) || 0;
            onSaveFinalValue(tournament.id, participantForFinalVal.id, val);
            setParticipantForFinalVal(null);
          }}
        />
      )}

      {participantForHacker && (
        <HackerTagModal
          participantName={participantForHacker.name}
          gameId={participantForHacker.gameIdName}
          onDismiss={() => setParticipantForHacker(null)}
          onConfirmed={(reason) => {
            onTagHacker(tournament.id, participantForHacker.id, reason);
            setParticipantForHacker(null);
          }}
        />
      )}

      {scheduleToFill && (
        <ScheduleValuesModal
          tournament={tournament}
          schedule={scheduleToFill}
          onDismiss={() => setScheduleToFill(null)}
          onSubmitValues={(valuesMap) => {
            onSubmitSchedule(tournament.id, scheduleToFill.id, valuesMap);
            setScheduleToFill(null);
          }}
        />
      )}

      {showReleaseModal && (
        <ReleaseModal
          assignmentId={tournament.id}
          onDismiss={() => setShowReleaseModal(false)}
          onConfirmRelease={(reason) => {
            onReleaseTournament(tournament.id, reason);
            setShowReleaseModal(false);
          }}
        />
      )}
    </div>
  );
};

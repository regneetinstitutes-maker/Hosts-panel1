import React, { useState } from 'react';
import {
  Gamepad2,
  KeyRound,
  Layers,
  Camera,
  Users,
  AlertTriangle,
  CheckCircle,
  Eye,
  EyeOff,
  Skull,
  LockOpen,
  Trophy,
  Coins
} from 'lucide-react';
import { OmbMatch, OmbParticipant } from '../types';
import { DoubleVerificationModal } from './DoubleVerificationModal';
import { HackerTagModal } from './HackerTagModal';
import { ScreenshotUploadModal } from './ScreenshotUploadModal';
import { ReleaseModal } from './ReleaseModal';

interface OmbRunningViewProps {
  match: OmbMatch | null;
  onUpdateRoomCredentials: (matchId: string, roomId: string, password: string) => void;
  onSaveParticipantKills: (matchId: string, participantId: string, kills: number) => void;
  onUploadScreenshot: (matchId: string, url: string) => void;
  onTagHacker: (matchId: string, participantId: string, reason: string) => void;
  onReleaseMatch: (matchId: string, reason: string) => void;
  onCompleteMatch: (matchId: string) => void;
}

export const OmbRunningView: React.FC<OmbRunningViewProps> = ({
  match,
  onUpdateRoomCredentials,
  onSaveParticipantKills,
  onUploadScreenshot,
  onTagHacker,
  onReleaseMatch,
  onCompleteMatch
}) => {
  const [showRoomDoubleVerify, setShowRoomDoubleVerify] = useState(false);
  const [showReleaseModal, setShowReleaseModal] = useState(false);
  const [showScreenshotModal, setShowScreenshotModal] = useState(false);
  const [participantForKills, setParticipantForKills] = useState<OmbParticipant | null>(null);
  const [participantForHacker, setParticipantForHacker] = useState<OmbParticipant | null>(null);
  const [showPassword, setShowPassword] = useState(false);

  if (!match) {
    return (
      <div className="text-center py-24 bg-slate-900/40 border border-slate-800/60 rounded-3xl max-w-4xl mx-auto my-12 px-6 space-y-4">
        <div className="w-14 h-14 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center mx-auto text-indigo-400">
          <Gamepad2 className="w-7 h-7" />
        </div>
        <h3 className="text-lg font-bold text-white">No Running Match Assignment</h3>
        <p className="text-xs text-slate-400 max-w-md mx-auto">
          You do not currently have a claimed OMB match. Switch to the <strong>Available</strong> tab to claim a lobby and begin room setup.
        </p>
      </div>
    );
  }

  const isCompleted = match.status === 'COMPLETED';
  const isCancelled = match.status === 'CANCELLED';
  const isInteractive = !isCompleted && !isCancelled;

  const allKillsRecorded = match.participants.every(
    (p) => p.isHackerCheater || (p.kills !== null && p.kills !== undefined)
  );
  const hasScreenshot = !!match.screenshotUrl;
  const canComplete = hasScreenshot && allKillsRecorded && isInteractive;

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 py-6">
      {/* Top Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-5 bg-slate-900/90 border border-slate-800 rounded-3xl shadow-xl">
        <div className="flex items-center gap-4">
          <div className="w-12 h-12 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
            <Gamepad2 className="w-6 h-6" />
          </div>
          <div>
            <div className="flex items-center gap-2.5">
              <h2 className="text-lg font-black text-white">Running OMB Assignment</h2>
              <span
                className={`text-[10px] font-bold px-2.5 py-0.5 rounded-full ${
                  match.status === 'RUNNING'
                    ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                    : match.status === 'COMPLETED'
                    ? 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
                    : 'bg-red-500/20 text-red-300 border border-red-500/30'
                }`}
              >
                {match.status}
              </span>
            </div>
            <div className="text-xs text-slate-400 mt-0.5">
              Match ID: <strong className="text-indigo-400 font-mono">#{match.id}</strong> • {match.game} ({match.mode})
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

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Left Column: Room Credentials & Match Details */}
        <div className="space-y-6 lg:col-span-1">
          {/* Room ID & Password Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs font-bold text-slate-300">
                <KeyRound className="w-4 h-4 text-indigo-400" />
                Room Credentials (Double Verified)
              </div>
              <span className="text-[10px] text-indigo-400 font-mono bg-indigo-500/10 px-2 py-0.5 rounded-md border border-indigo-500/20">
                2-Step Auth
              </span>
            </div>

            {match.roomId && match.roomPassword ? (
              <div className="space-y-3 p-4 rounded-2xl bg-slate-950/60 border border-slate-800/80">
                <div className="flex items-center justify-between text-xs">
                  <span className="text-slate-400">Room ID:</span>
                  <span className="font-mono font-bold text-white text-sm tracking-wider">
                    {match.roomId}
                  </span>
                </div>
                <div className="flex items-center justify-between text-xs pt-2 border-t border-slate-800/60">
                  <span className="text-slate-400">Password:</span>
                  <div className="flex items-center gap-2">
                    <span className="font-mono font-bold text-indigo-300 text-sm">
                      {showPassword ? match.roomPassword : '••••••••'}
                    </span>
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="text-slate-400 hover:text-white transition"
                    >
                      {showPassword ? <EyeOff className="w-3.5 h-3.5" /> : <Eye className="w-3.5 h-3.5" />}
                    </button>
                  </div>
                </div>
              </div>
            ) : (
              <div className="p-4 rounded-2xl bg-amber-950/20 border border-amber-900/40 text-xs text-amber-300">
                ⚠️ Room credentials have not been configured yet. In-game participants cannot join until Room ID and Password are confirmed.
              </div>
            )}

            {isInteractive && (
              <button
                type="button"
                onClick={() => setShowRoomDoubleVerify(true)}
                className="w-full py-2.5 px-3 rounded-xl bg-indigo-600 hover:bg-indigo-500 text-white text-xs font-bold transition flex items-center justify-center gap-2 cursor-pointer shadow-md shadow-indigo-600/20"
              >
                <KeyRound className="w-3.5 h-3.5" />
                {match.roomId ? 'Re-enter / Update Credentials' : 'Set Room ID & Password'}
              </button>
            )}
          </div>

          {/* Screenshot Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-2 text-xs font-bold text-slate-300">
                <Camera className="w-4 h-4 text-indigo-400" />
                Match Result Scoreboard
              </div>
              {hasScreenshot && (
                <span className="text-[10px] text-emerald-400 font-bold flex items-center gap-1">
                  <CheckCircle className="w-3 h-3" /> Uploaded
                </span>
              )}
            </div>

            {hasScreenshot ? (
              <div className="p-3.5 rounded-2xl bg-slate-950/60 border border-slate-800 space-y-2">
                <div className="text-[11px] text-slate-400">Scoreboard capture attached:</div>
                <div className="text-xs font-mono text-indigo-300 break-all bg-slate-900 p-2 rounded-xl border border-slate-800">
                  {match.screenshotUrl}
                </div>
              </div>
            ) : (
              <div className="text-xs text-slate-400 p-3.5 rounded-2xl bg-slate-950/40 border border-slate-800/60">
                Overall match scoreboard capture is mandatory before results can be approved and finalized.
              </div>
            )}

            {isInteractive && (
              <button
                type="button"
                onClick={() => setShowScreenshotModal(true)}
                className="w-full py-2.5 px-3 rounded-xl bg-slate-800 hover:bg-slate-700 text-white text-xs font-bold transition flex items-center justify-center gap-2 cursor-pointer"
              >
                <Camera className="w-3.5 h-3.5" />
                {hasScreenshot ? 'Change Scoreboard Image' : 'Upload Match Screenshot'}
              </button>
            )}
          </div>

          {/* Completion Status Card */}
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
            <div className="text-xs font-bold text-slate-300 flex items-center gap-2">
              <Trophy className="w-4 h-4 text-emerald-400" />
              Match Completion Checklist
            </div>

            <div className="space-y-2 text-xs">
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950/40 border border-slate-800/60">
                <span className="text-slate-300">Room Credentials Set:</span>
                <span className={match.roomId ? 'text-emerald-400 font-bold' : 'text-slate-500'}>
                  {match.roomId ? 'Yes ✅' : 'Pending'}
                </span>
              </div>
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950/40 border border-slate-800/60">
                <span className="text-slate-300">All Participant Kills Recorded:</span>
                <span className={allKillsRecorded ? 'text-emerald-400 font-bold' : 'text-slate-500'}>
                  {allKillsRecorded ? 'Yes ✅' : 'Pending'}
                </span>
              </div>
              <div className="flex items-center justify-between p-2.5 rounded-xl bg-slate-950/40 border border-slate-800/60">
                <span className="text-slate-300">Result Scoreboard Uploaded:</span>
                <span className={hasScreenshot ? 'text-emerald-400 font-bold' : 'text-slate-500'}>
                  {hasScreenshot ? 'Yes ✅' : 'Pending'}
                </span>
              </div>
            </div>

            {isInteractive ? (
              <button
                type="button"
                disabled={!canComplete}
                onClick={() => onCompleteMatch(match.id)}
                className={`w-full py-3 rounded-xl text-xs font-bold uppercase tracking-wider transition flex items-center justify-center gap-2 cursor-pointer shadow-lg ${
                  canComplete
                    ? 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-emerald-600/25'
                    : 'bg-slate-800 text-slate-500 cursor-not-allowed shadow-none'
                }`}
              >
                <CheckCircle className="w-4 h-4" />
                Complete Match & Issue Payouts
              </button>
            ) : (
              <div className="p-3 text-center rounded-xl bg-emerald-950/40 border border-emerald-800/60 text-emerald-300 text-xs font-bold">
                ✓ Match is Finalized and Completed
              </div>
            )}
          </div>
        </div>

        {/* Right Column: Participant Kill Verification Table */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-slate-900/80 border border-slate-800 rounded-3xl p-5 shadow-xl space-y-4">
            <div className="flex items-center justify-between">
              <div>
                <h3 className="text-base font-bold text-white flex items-center gap-2">
                  <Users className="w-4 h-4 text-indigo-400" />
                  Participant Verification & Kills
                </h3>
                <p className="text-xs text-slate-400">
                  Double verification is required when submitting participant kills to prevent typos.
                </p>
              </div>
              <div className="text-xs text-slate-300 font-semibold bg-slate-950 px-3 py-1.5 rounded-xl border border-slate-800">
                ₹{match.perKillPrize} / Kill
              </div>
            </div>

            {/* List */}
            <div className="space-y-3">
              {match.participants.map((p) => {
                const isHacker = p.isHackerCheater;
                return (
                  <div
                    key={p.id}
                    className={`p-4 rounded-2xl border transition flex flex-col sm:flex-row sm:items-center justify-between gap-3 ${
                      isHacker
                        ? 'bg-red-950/20 border-red-900/40'
                        : 'bg-slate-950/60 border-slate-800 hover:border-slate-700/80'
                    }`}
                  >
                    <div>
                      <div className="flex items-center gap-2">
                        <span className="text-xs font-mono font-bold text-indigo-400 bg-indigo-500/10 px-2 py-0.5 rounded-md border border-indigo-500/20">
                          Slot #{p.slotNumber}
                        </span>
                        <span className="text-sm font-bold text-white">{p.name}</span>
                        {isHacker && (
                          <span className="text-[10px] font-extrabold uppercase px-2 py-0.5 rounded-md bg-red-600 text-white">
                            HACKER
                          </span>
                        )}
                      </div>
                      <div className="text-xs text-slate-400 mt-1 flex items-center gap-2">
                        <span>In-Game: <strong className="text-slate-300 font-mono">{p.gameIdName}</strong></span>
                        <span>•</span>
                        <span>UID: <span className="font-mono text-slate-400">{p.inGameUid}</span></span>
                      </div>
                      {isHacker && (
                        <div className="text-[11px] text-red-300 font-semibold mt-1">
                          Violation: {p.hackerReason || 'Cheating detected'} • Payout: ₹0 (Forfeited)
                        </div>
                      )}
                    </div>

                    <div className="flex items-center gap-3">
                      {/* Kills Box */}
                      {isHacker ? (
                        <div className="text-xs font-bold text-red-400 px-3 py-1.5 rounded-xl bg-red-950/40 border border-red-900/60">
                          Disqualified
                        </div>
                      ) : p.kills !== null && p.kills !== undefined ? (
                        <div className="text-right">
                          <button
                            type="button"
                            disabled={!isInteractive}
                            onClick={() => setParticipantForKills(p)}
                            className="px-3.5 py-1.5 rounded-xl bg-slate-900 border border-emerald-500/40 text-emerald-400 text-xs font-bold flex items-center gap-1.5 hover:bg-slate-800 transition cursor-pointer"
                          >
                            <span>{p.kills} Kills ✅</span>
                            <span className="text-[10px] text-slate-400 font-normal">
                              (₹{p.kills * match.perKillPrize})
                            </span>
                          </button>
                        </div>
                      ) : (
                        <button
                          type="button"
                          disabled={!isInteractive}
                          onClick={() => setParticipantForKills(p)}
                          className="px-3.5 py-2 rounded-xl bg-indigo-600/90 hover:bg-indigo-500 text-white text-xs font-bold transition flex items-center gap-1 cursor-pointer shadow-sm"
                        >
                          + Enter Kills
                        </button>
                      )}

                      {/* Hacker Flag Button */}
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
        </div>
      </div>

      {/* Modals */}
      {showRoomDoubleVerify && (
        <DoubleVerificationModal
          title="Setup In-Game Room Credentials"
          primaryFieldLabel="Room ID"
          secondaryFieldLabel="Room Password"
          isNumeric={false}
          onDismiss={() => setShowRoomDoubleVerify(false)}
          onConfirmed={(roomId, roomPassword) => {
            onUpdateRoomCredentials(match.id, roomId, roomPassword);
            setShowRoomDoubleVerify(false);
          }}
        />
      )}

      {participantForKills && (
        <DoubleVerificationModal
          title={`Enter Kills: ${participantForKills.name}`}
          primaryFieldLabel="Total Kills"
          isNumeric={true}
          onDismiss={() => setParticipantForKills(null)}
          onConfirmed={(killsStr) => {
            const kills = parseInt(killsStr, 10) || 0;
            onSaveParticipantKills(match.id, participantForKills.id, kills);
            setParticipantForKills(null);
          }}
        />
      )}

      {participantForHacker && (
        <HackerTagModal
          participantName={participantForHacker.name}
          gameId={participantForHacker.gameIdName}
          onDismiss={() => setParticipantForHacker(null)}
          onConfirmed={(reason) => {
            onTagHacker(match.id, participantForHacker.id, reason);
            setParticipantForHacker(null);
          }}
        />
      )}

      {showScreenshotModal && (
        <ScreenshotUploadModal
          matchId={match.id}
          currentScreenshotUrl={match.screenshotUrl}
          onDismiss={() => setShowScreenshotModal(false)}
          onUploadScreenshot={(url) => {
            onUploadScreenshot(match.id, url);
            setShowScreenshotModal(false);
          }}
        />
      )}

      {showReleaseModal && (
        <ReleaseModal
          assignmentId={match.id}
          onDismiss={() => setShowReleaseModal(false)}
          onConfirmRelease={(reason) => {
            onReleaseMatch(match.id, reason);
            setShowReleaseModal(false);
          }}
        />
      )}
    </div>
  );
};

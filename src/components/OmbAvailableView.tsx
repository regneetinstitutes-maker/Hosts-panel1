import React from 'react';
import { Gamepad2, Users, Coins, Info, ShieldAlert, RefreshCw, CheckCircle2 } from 'lucide-react';
import { OmbMatch, HostUser } from '../types';

interface OmbAvailableViewProps {
  availableOmbs: OmbMatch[];
  currentHost: HostUser;
  onClaimOmb: (matchId: string) => void;
  onRefresh: () => void;
}

export const OmbAvailableView: React.FC<OmbAvailableViewProps> = ({
  availableOmbs,
  currentHost,
  onClaimOmb,
  onRefresh
}) => {
  const hasActiveAssignment = !!currentHost.currentAssignmentId;
  const isHostDisabled = currentHost.status === 'DISABLED';
  const isClaimDisabled = hasActiveAssignment || isHostDisabled;

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 py-6">
      {/* Top title & refresh */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-black text-white tracking-tight flex items-center gap-2">
            Available OMB Matches
            <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
              {availableOmbs.length} Available
            </span>
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            Real-time One Match Battle lobbies awaiting host room deployment.
          </p>
        </div>

        <button
          onClick={onRefresh}
          className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer flex items-center gap-1.5 text-xs font-semibold"
          title="Refresh available lobbies"
        >
          <RefreshCw className="w-4 h-4" />
          <span className="hidden sm:inline">Refresh</span>
        </button>
      </div>

      {/* Active Assignment Restriction Banner */}
      {hasActiveAssignment && (
        <div className="p-4 rounded-2xl bg-amber-950/40 border border-amber-800/60 flex items-start gap-3">
          <Info className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
          <div className="text-xs text-amber-200">
            <strong>Active Assignment Active:</strong> You currently have an active running assignment (
            <span className="font-mono font-bold text-amber-300">#{currentHost.currentAssignmentId}</span>).
            Under tournament rules, you must either complete it or release it before you can claim another OMB match.
          </div>
        </div>
      )}

      {/* Disabled Host Banner */}
      {isHostDisabled && (
        <div className="p-4 rounded-2xl bg-red-950/40 border border-red-800/60 flex items-start gap-3">
          <ShieldAlert className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
          <div className="text-xs text-red-200">
            <strong>Host Account Suspended:</strong> Your host account is disabled by administrators. Claiming matches is disabled until account status is restored.
          </div>
        </div>
      )}

      {/* Grid of Matches */}
      {availableOmbs.length === 0 ? (
        <div className="text-center py-20 bg-slate-900/40 border border-slate-800/60 rounded-3xl space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-slate-800/60 flex items-center justify-center mx-auto text-slate-500">
            <Gamepad2 className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-slate-200">No OMB Matches Currently Available</h3>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            All current lobbies are claimed or scheduled. Check back shortly or click refresh to query the backend.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          {availableOmbs.map((match) => {
            return (
              <div
                key={match.id}
                className="bg-slate-900/80 border border-slate-800 hover:border-slate-700/80 rounded-3xl p-5 flex flex-col justify-between shadow-xl transition space-y-4"
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="text-xs font-mono font-bold text-indigo-400">#{match.id}</div>
                      <h3 className="text-base font-bold text-white mt-0.5">{match.game}</h3>
                    </div>
                    <span className="text-[11px] font-bold px-2.5 py-1 rounded-xl bg-indigo-500/10 text-indigo-300 border border-indigo-500/20">
                      {match.mode}
                    </span>
                  </div>

                  <div className="grid grid-cols-2 gap-2 mt-4">
                    <div className="p-2.5 rounded-2xl bg-slate-950/60 border border-slate-800/60">
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">Entry Fee</div>
                      <div className="text-sm font-bold text-emerald-400 mt-0.5">₹{match.entryFee}</div>
                    </div>
                    <div className="p-2.5 rounded-2xl bg-slate-950/60 border border-slate-800/60">
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">Per Kill Prize</div>
                      <div className="text-sm font-bold text-indigo-300 mt-0.5">₹{match.perKillPrize}</div>
                    </div>
                  </div>

                  <div className="mt-3 flex items-center justify-between text-xs text-slate-400 py-1 border-t border-slate-800/60">
                    <span className="flex items-center gap-1.5">
                      <Users className="w-3.5 h-3.5 text-slate-500" />
                      Participants Joined:
                    </span>
                    <span className="font-bold text-white">
                      {match.participantsJoined} / {match.maxParticipants}
                    </span>
                  </div>

                  <div className="flex items-center justify-between text-xs text-slate-400 py-1">
                    <span>Allocated Slots:</span>
                    <span className="font-mono text-[11px] text-indigo-300">
                      {match.slotNumbers.join(', ')}
                    </span>
                  </div>
                </div>

                <button
                  type="button"
                  disabled={isClaimDisabled}
                  onClick={() => onClaimOmb(match.id)}
                  className={`w-full py-2.5 px-4 rounded-xl text-xs font-bold uppercase tracking-wider transition flex items-center justify-center gap-2 cursor-pointer shadow-md ${
                    isClaimDisabled
                      ? 'bg-slate-800 text-slate-500 cursor-not-allowed shadow-none'
                      : 'bg-indigo-600 hover:bg-indigo-500 text-white shadow-indigo-600/25'
                  }`}
                >
                  <Gamepad2 className="w-4 h-4" />
                  Claim Match
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

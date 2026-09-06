import React from 'react';
import { Trophy, Users, Clock, Calendar, Info, ShieldAlert, RefreshCw, Award } from 'lucide-react';
import { Tournament, HostUser } from '../types';

interface TournamentAvailableViewProps {
  availableTournaments: Tournament[];
  currentHost: HostUser;
  onClaimTournament: (tourneyId: string) => void;
  onRefresh: () => void;
}

export const TournamentAvailableView: React.FC<TournamentAvailableViewProps> = ({
  availableTournaments,
  currentHost,
  onClaimTournament,
  onRefresh
}) => {
  const hasActiveAssignment = !!currentHost.currentAssignmentId;
  const isHostDisabled = currentHost.status === 'DISABLED';
  const isClaimDisabled = hasActiveAssignment || isHostDisabled;

  return (
    <div className="space-y-6 max-w-7xl mx-auto px-4 py-6">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div>
          <h2 className="text-xl font-black text-white tracking-tight flex items-center gap-2">
            Available Esports Tournaments
            <span className="text-xs font-bold px-2.5 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
              {availableTournaments.length} Scheduled
            </span>
          </h2>
          <p className="text-xs text-slate-400 mt-0.5">
            High-stakes competitive tournaments awaiting host claim and verification.
          </p>
        </div>

        <button
          onClick={onRefresh}
          className="p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer flex items-center gap-1.5 text-xs font-semibold"
          title="Refresh tournaments"
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
            Complete or release your active assignment before claiming a new tournament.
          </div>
        </div>
      )}

      {/* Disabled Host Banner */}
      {isHostDisabled && (
        <div className="p-4 rounded-2xl bg-red-950/40 border border-red-800/60 flex items-start gap-3">
          <ShieldAlert className="w-5 h-5 text-red-400 shrink-0 mt-0.5" />
          <div className="text-xs text-red-200">
            <strong>Host Account Disabled:</strong> You cannot claim tournaments while your host account is disabled by Admin.
          </div>
        </div>
      )}

      {/* List */}
      {availableTournaments.length === 0 ? (
        <div className="text-center py-20 bg-slate-900/40 border border-slate-800/60 rounded-3xl space-y-3">
          <div className="w-12 h-12 rounded-2xl bg-slate-800/60 flex items-center justify-center mx-auto text-slate-500">
            <Trophy className="w-6 h-6" />
          </div>
          <h3 className="text-base font-bold text-slate-200">No Tournaments Currently Available</h3>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            Check back soon as tournaments are scheduled by tournament administrators.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {availableTournaments.map((tourney) => {
            return (
              <div
                key={tourney.id}
                className="bg-slate-900/80 border border-slate-800 hover:border-slate-700/80 rounded-3xl p-6 flex flex-col justify-between shadow-xl transition space-y-4"
              >
                <div>
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <div className="text-xs font-mono font-black text-emerald-400">#{tourney.id}</div>
                      <h3 className="text-lg font-bold text-white mt-0.5">{tourney.game}</h3>
                    </div>
                    <span className="text-[11px] font-bold px-3 py-1 rounded-xl bg-emerald-500/10 text-emerald-300 border border-emerald-500/20">
                      {tourney.mode}
                    </span>
                  </div>

                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2.5 mt-4">
                    <div className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800/60">
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">Metric</div>
                      <div className="text-xs font-bold text-indigo-400 mt-1 truncate">{tourney.tournamentMetric}</div>
                    </div>
                    <div className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800/60">
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">Entry Fee</div>
                      <div className="text-xs font-bold text-emerald-400 mt-1">₹{tourney.entryFee}</div>
                    </div>
                    <div className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800/60">
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">Duration</div>
                      <div className="text-xs font-bold text-white mt-1">{tourney.durationMinutes} mins</div>
                    </div>
                    <div className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800/60">
                      <div className="text-[10px] text-slate-400 uppercase font-semibold">Results On</div>
                      <div className="text-xs font-bold text-slate-300 mt-1 truncate">{tourney.resultsOn}</div>
                    </div>
                  </div>

                  <div className="mt-4 p-3 rounded-2xl bg-slate-950/40 border border-slate-800/40 text-xs text-slate-300 space-y-1.5">
                    <div className="flex items-center justify-between">
                      <span className="flex items-center gap-1 text-slate-400">
                        <Users className="w-3.5 h-3.5 text-slate-500" />
                        Participants:
                      </span>
                      <span className="font-bold text-white">
                        {tourney.participantsJoined} / {tourney.maxParticipants} ({tourney.teamSize})
                      </span>
                    </div>

                    <div className="flex items-center justify-between text-[11px] pt-1 border-t border-slate-800/60">
                      <span className="text-slate-400 flex items-center gap-1">
                        <Award className="w-3.5 h-3.5 text-amber-400" />
                        Prize Pool:
                      </span>
                      <span className="text-amber-300 font-semibold">
                        {tourney.prizeChart.map((p) => `Rank ${p.rank}: ₹${p.prize}`).join(', ')}
                      </span>
                    </div>
                  </div>
                </div>

                <button
                  type="button"
                  disabled={isClaimDisabled}
                  onClick={() => onClaimTournament(tourney.id)}
                  className={`w-full py-3 px-4 rounded-xl text-xs font-bold uppercase tracking-wider transition flex items-center justify-center gap-2 cursor-pointer shadow-md ${
                    isClaimDisabled
                      ? 'bg-slate-800 text-slate-500 cursor-not-allowed shadow-none'
                      : 'bg-emerald-600 hover:bg-emerald-500 text-white shadow-emerald-600/25'
                  }`}
                >
                  <Trophy className="w-4 h-4" />
                  Claim Tournament
                </button>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

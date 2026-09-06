import React, { useState } from 'react';
import { Calendar, X, Trophy } from 'lucide-react';
import { Tournament, TournamentSchedule } from '../types';

interface ScheduleValuesModalProps {
  tournament: Tournament;
  schedule: TournamentSchedule;
  onDismiss: () => void;
  onSubmitValues: (values: Record<string, number>) => void;
}

export const ScheduleValuesModal: React.FC<ScheduleValuesModalProps> = ({
  tournament,
  schedule,
  onDismiss,
  onSubmitValues
}) => {
  const [values, setValues] = useState<Record<string, string>>(() => {
    const init: Record<string, string> = {};
    tournament.participants.forEach((p) => {
      init[p.id] = '';
    });
    return init;
  });

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const result: Record<string, number> = {};
    tournament.participants.forEach((p) => {
      const val = parseInt(values[p.id], 10);
      result[p.id] = isNaN(val) ? 0 : val;
    });
    onSubmitValues(result);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-xs">
      <div className="w-full max-w-lg bg-slate-900 border border-slate-800 rounded-3xl shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-2xl bg-emerald-500/10 border border-emerald-500/20 flex items-center justify-center text-emerald-400">
              <Calendar className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-bold text-white">Enter Standings: {schedule.name}</h3>
              <p className="text-xs text-emerald-400">Metric: {tournament.tournamentMetric} (Snapshot)</p>
            </div>
          </div>
          <button
            onClick={onDismiss}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <form onSubmit={handleSubmit} className="p-6 space-y-4">
          <p className="text-xs text-slate-400">
            Enter current metric points for each competitor at this scheduled milestone. Ranks and interim leaderboard will be calculated and published.
          </p>

          <div className="max-h-72 overflow-y-auto space-y-2.5 pr-1">
            {tournament.participants.map((p) => (
              <div
                key={p.id}
                className="p-3 rounded-2xl bg-slate-950/60 border border-slate-800 flex items-center justify-between gap-3"
              >
                <div>
                  <div className="text-xs font-bold text-white">{p.name}</div>
                  <div className="text-[11px] text-slate-400 font-mono">{p.gameIdName}</div>
                </div>
                <div className="flex items-center gap-2">
                  <input
                    type="number"
                    value={values[p.id] ?? ''}
                    onChange={(e) => setValues({ ...values, [p.id]: e.target.value })}
                    placeholder="0"
                    className="w-24 px-3 py-1.5 bg-slate-900 border border-slate-700 rounded-xl text-white text-xs font-bold text-right focus:outline-hidden focus:border-emerald-500 transition"
                  />
                  <span className="text-[11px] text-slate-500 font-semibold">pts</span>
                </div>
              </div>
            ))}
          </div>

          <div className="flex items-center justify-end gap-2.5 pt-4 border-t border-slate-800">
            <button
              type="button"
              onClick={onDismiss}
              className="px-4 py-2 text-xs font-medium text-slate-400 hover:text-white transition"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-bold flex items-center gap-1.5 shadow-md shadow-emerald-600/20 transition cursor-pointer"
            >
              <Trophy className="w-3.5 h-3.5" />
              Publish Chart
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

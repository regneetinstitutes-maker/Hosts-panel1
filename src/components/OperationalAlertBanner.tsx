import React, { useState } from 'react';
import { AlertCircle, AlertTriangle, Info, ChevronDown, ChevronUp } from 'lucide-react';
import { OperationalAlert } from '../types';

interface OperationalAlertBannerProps {
  alerts: OperationalAlert[];
}

export const OperationalAlertBanner: React.FC<OperationalAlertBannerProps> = ({ alerts }) => {
  const [isExpanded, setIsExpanded] = useState(false);

  if (!alerts || alerts.length === 0) return null;

  const latestAlert = alerts[0];

  const getAlertStyles = (severity: string) => {
    switch (severity) {
      case 'CRITICAL':
        return {
          bg: 'bg-red-950/40 border-red-900/50 text-red-300',
          icon: <AlertCircle className="w-4 h-4 text-red-400 shrink-0" />,
          badge: 'bg-red-500/20 text-red-300 border border-red-500/30'
        };
      case 'WARNING':
        return {
          bg: 'bg-amber-950/40 border-amber-900/50 text-amber-200',
          icon: <AlertTriangle className="w-4 h-4 text-amber-400 shrink-0" />,
          badge: 'bg-amber-500/20 text-amber-300 border border-amber-500/30'
        };
      default:
        return {
          bg: 'bg-blue-950/40 border-blue-900/50 text-blue-200',
          icon: <Info className="w-4 h-4 text-blue-400 shrink-0" />,
          badge: 'bg-blue-500/20 text-blue-300 border border-blue-500/30'
        };
    }
  };

  const style = getAlertStyles(latestAlert.severity);

  return (
    <div className="px-4 py-2">
      <div className={`rounded-2xl border p-3.5 transition ${style.bg}`}>
        <div className="flex items-center justify-between gap-3 cursor-pointer" onClick={() => setIsExpanded(!isExpanded)}>
          <div className="flex items-center gap-2.5 flex-1 min-w-0">
            {style.icon}
            <div className="flex items-center gap-2 flex-wrap">
              <span className={`text-[10px] uppercase font-bold tracking-wider px-2 py-0.5 rounded-md ${style.badge}`}>
                {latestAlert.severity} ALERT
              </span>
              {alerts.length > 1 && (
                <span className="text-[10px] bg-red-600 text-white font-bold px-1.5 py-0.2 rounded-full">
                  {alerts.length}
                </span>
              )}
              <span className="text-xs font-medium text-slate-200 truncate">
                {latestAlert.message}
              </span>
            </div>
          </div>
          <button
            type="button"
            className="text-slate-400 hover:text-white p-1 transition"
          >
            {isExpanded ? <ChevronUp className="w-4 h-4" /> : <ChevronDown className="w-4 h-4" />}
          </button>
        </div>

        {isExpanded && (
          <div className="mt-3 pt-3 border-t border-slate-800/80 space-y-2">
            <div className="text-[11px] font-bold text-slate-400">Alert Details Log:</div>
            <div className="space-y-1.5 max-h-40 overflow-y-auto">
              {alerts.map((alert) => (
                <div key={alert.id} className="text-xs text-slate-300 flex items-start gap-2">
                  <span className="text-slate-500 font-mono text-[10px] shrink-0 mt-0.5">
                    [{new Date(alert.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit', second: '2-digit' })}]
                  </span>
                  <span>
                    <strong className="text-white">{alert.matchOrTournamentId}</strong> ({alert.matchDetails}): {alert.message}
                  </span>
                </div>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

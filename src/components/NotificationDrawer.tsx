import React from 'react';
import { Bell, X, Gamepad2, Trophy, Clock } from 'lucide-react';
import { HostNotification } from '../types';

interface NotificationDrawerProps {
  notifications: HostNotification[];
  onClaim: (matchId: string, type: 'OMB' | 'TOURNAMENT') => void;
  onDismiss: () => void;
}

export const NotificationDrawer: React.FC<NotificationDrawerProps> = ({
  notifications,
  onClaim,
  onDismiss
}) => {
  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/60 backdrop-blur-xs">
      <div className="w-full max-w-md bg-slate-900 border-l border-slate-800 h-full flex flex-col shadow-2xl animate-in slide-in-from-right duration-200">
        {/* Header */}
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/80">
          <div className="flex items-center gap-3">
            <div className="w-9 h-9 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
              <Bell className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white">Push Notifications</h3>
              <p className="text-xs text-slate-400">Assignments & Schedule Alerts</p>
            </div>
          </div>
          <button
            onClick={onDismiss}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* List */}
        <div className="flex-1 overflow-y-auto p-4 space-y-3">
          {notifications.length === 0 ? (
            <div className="text-center py-16 text-slate-500 text-xs">
              No pending notifications.
            </div>
          ) : (
            notifications.map((notif) => (
              <div
                key={notif.id}
                className="p-4 rounded-2xl bg-slate-950/60 border border-slate-800 hover:border-slate-700 transition space-y-2.5"
              >
                <div className="flex items-start justify-between gap-2">
                  <div className="flex items-center gap-2">
                    {notif.type === 'OMB' ? (
                      <span className="p-1.5 rounded-lg bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
                        <Gamepad2 className="w-3.5 h-3.5" />
                      </span>
                    ) : (
                      <span className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
                        <Trophy className="w-3.5 h-3.5" />
                      </span>
                    )}
                    <span className="text-xs font-bold text-white">{notif.title}</span>
                  </div>
                  <span className="text-[10px] text-slate-500 flex items-center gap-1 font-mono">
                    <Clock className="w-3 h-3" />
                    {new Date(notif.timestamp).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}
                  </span>
                </div>

                <p className="text-xs text-slate-300">{notif.message}</p>

                <div className="flex items-center justify-between text-[11px] text-slate-400 pt-1">
                  <span>
                    ID: <strong className="text-white font-mono">{notif.matchId}</strong>
                  </span>
                  <span>
                    Entry: <strong className="text-emerald-400">₹{notif.entryFee}</strong>
                  </span>
                </div>

                <button
                  onClick={() => {
                    onClaim(notif.matchId, notif.type);
                    onDismiss();
                  }}
                  className="w-full py-2 px-3 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold shadow-sm transition cursor-pointer"
                >
                  Claim Assignment Now
                </button>
              </div>
            ))
          )}
        </div>

        {/* Footer */}
        <div className="p-4 border-t border-slate-800">
          <button
            onClick={onDismiss}
            className="w-full py-2.5 bg-slate-800 hover:bg-slate-700 text-slate-200 rounded-xl text-xs font-semibold transition"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

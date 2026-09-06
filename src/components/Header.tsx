import React from 'react';
import { Gamepad2, Trophy, Bell, LogOut, ShieldAlert, RotateCcw } from 'lucide-react';
import { HostUser } from '../types';

interface HeaderProps {
  host: HostUser;
  unreadNotificationsCount: number;
  onOpenNotifications: () => void;
  onLogout: () => void;
  onResetData: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  host,
  unreadNotificationsCount,
  onOpenNotifications,
  onLogout,
  onResetData
}) => {
  const isOmbHost = host.role === 'OMB_HOST';

  return (
    <header className="bg-slate-900/90 border-b border-slate-800 sticky top-0 z-40 backdrop-blur-md px-4 py-3">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-4">
        {/* Host Identity */}
        <div className="flex items-center gap-3">
          <div
            className={`w-10 h-10 rounded-2xl flex items-center justify-center border shadow-xs ${
              isOmbHost
                ? 'bg-indigo-500/10 border-indigo-500/30 text-indigo-400'
                : 'bg-emerald-500/10 border-emerald-500/30 text-emerald-400'
            }`}
          >
            {isOmbHost ? <Gamepad2 className="w-5 h-5" /> : <Trophy className="w-5 h-5" />}
          </div>

          <div>
            <div className="flex items-center gap-2">
              <span className="text-sm font-bold text-white tracking-tight">{host.fullName}</span>
              <span
                className={`text-[10px] font-bold px-2 py-0.5 rounded-full ${
                  host.status === 'ACTIVE'
                    ? 'bg-emerald-500/20 text-emerald-300 border border-emerald-500/30'
                    : 'bg-red-500/20 text-red-300 border border-red-500/30'
                }`}
              >
                {host.status}
              </span>
            </div>
            <div className="text-xs text-slate-400 flex items-center gap-2">
              <span>{isOmbHost ? 'One Match Battle Host' : 'Esports Tournament Host'}</span>
              <span className="text-slate-600">•</span>
              <span className="font-mono text-[11px] text-slate-400">{host.id}</span>
            </div>
          </div>
        </div>

        {/* Actions */}
        <div className="flex items-center gap-2">
          <button
            onClick={onResetData}
            title="Reset Mock State"
            className="p-2 text-slate-400 hover:text-slate-200 hover:bg-slate-800 rounded-xl transition cursor-pointer text-xs flex items-center gap-1"
          >
            <RotateCcw className="w-4 h-4" />
            <span className="hidden sm:inline">Reset</span>
          </button>

          <button
            onClick={onOpenNotifications}
            className="relative p-2 text-slate-400 hover:text-white hover:bg-slate-800 rounded-xl transition cursor-pointer"
            title="Push Notifications"
          >
            <Bell className="w-5 h-5" />
            {unreadNotificationsCount > 0 && (
              <span className="absolute top-1 right-1 w-4 h-4 bg-indigo-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center shadow-xs">
                {unreadNotificationsCount}
              </span>
            )}
          </button>

          <button
            onClick={onLogout}
            className="p-2 text-slate-400 hover:text-red-400 hover:bg-slate-800 rounded-xl transition cursor-pointer"
            title="Logout"
          >
            <LogOut className="w-5 h-5" />
          </button>
        </div>
      </div>
    </header>
  );
};

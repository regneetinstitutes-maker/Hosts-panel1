import React, { useState } from 'react';
import { ShieldCheck, Gamepad2, Trophy, KeyRound, User, Lock, AlertCircle, ArrowRight } from 'lucide-react';
import { HostRole } from '../types';

interface LoginViewProps {
  onLogin: (hostId: string, password: string, role: HostRole) => void;
  error?: string | null;
}

export const LoginView: React.FC<LoginViewProps> = ({ onLogin, error }) => {
  const [hostId, setHostId] = useState('HOST_OMB_01');
  const [password, setPassword] = useState('host123');
  const [role, setRole] = useState<HostRole>('OMB_HOST');

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    onLogin(hostId.trim(), password.trim(), role);
  };

  const handleQuickLogin = (presetId: string, presetRole: HostRole) => {
    setHostId(presetId);
    setPassword('host123');
    setRole(presetRole);
    onLogin(presetId, 'host123', presetRole);
  };

  return (
    <div className="min-h-screen flex flex-col justify-center items-center px-4 py-12 bg-radial from-slate-900 via-slate-950 to-black">
      <div className="w-full max-w-md space-y-6">
        {/* Brand Logo & Header */}
        <div className="text-center space-y-2">
          <div className="inline-flex p-3 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 shadow-xl mb-1">
            <ShieldCheck className="w-8 h-8" />
          </div>
          <h1 className="text-2xl font-black tracking-tight text-white">Host Operations Panel</h1>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            Authorized portal for OMB rooms, Esports tournaments, double verification, and payout processing.
          </p>
        </div>

        {/* Card */}
        <div className="p-7 bg-slate-900/90 border border-slate-800/80 rounded-3xl shadow-2xl backdrop-blur-xl space-y-6">
          {error && (
            <div className="p-3.5 rounded-xl bg-red-950/50 border border-red-800/60 flex items-start gap-2.5 text-xs text-red-200">
              <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
              <span>{error}</span>
            </div>
          )}

          {/* Role selector tab */}
          <div>
            <label className="block text-xs font-semibold text-slate-400 mb-2">Select Host Assignment Role</label>
            <div className="grid grid-cols-2 gap-2 p-1 bg-slate-950 rounded-2xl border border-slate-800">
              <button
                type="button"
                onClick={() => {
                  setRole('OMB_HOST');
                  if (hostId === 'HOST_TOURN_01') setHostId('HOST_OMB_01');
                }}
                className={`py-2.5 px-3 rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition cursor-pointer ${
                  role === 'OMB_HOST'
                    ? 'bg-indigo-600 text-white shadow-md shadow-indigo-600/30'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <Gamepad2 className="w-4 h-4" />
                OMB Host
              </button>
              <button
                type="button"
                onClick={() => {
                  setRole('TOURNAMENT_HOST');
                  if (hostId === 'HOST_OMB_01') setHostId('HOST_TOURN_01');
                }}
                className={`py-2.5 px-3 rounded-xl text-xs font-bold flex items-center justify-center gap-2 transition cursor-pointer ${
                  role === 'TOURNAMENT_HOST'
                    ? 'bg-emerald-600 text-white shadow-md shadow-emerald-600/30'
                    : 'text-slate-400 hover:text-slate-200'
                }`}
              >
                <Trophy className="w-4 h-4" />
                Tournament Host
              </button>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Host ID / Staff Identifier</label>
              <div className="relative">
                <User className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                <input
                  type="text"
                  value={hostId}
                  onChange={(e) => setHostId(e.target.value)}
                  placeholder="e.g. HOST_OMB_01"
                  className="w-full pl-10 pr-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-xs font-mono focus:outline-hidden focus:border-indigo-500 transition"
                  required
                />
              </div>
            </div>

            <div>
              <label className="block text-xs font-semibold text-slate-300 mb-1.5">Security Password</label>
              <div className="relative">
                <Lock className="w-4 h-4 text-slate-500 absolute left-3.5 top-3" />
                <input
                  type="password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  className="w-full pl-10 pr-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-xs focus:outline-hidden focus:border-indigo-500 transition"
                  required
                />
              </div>
            </div>

            <button
              type="submit"
              className={`w-full py-3 rounded-xl text-xs font-bold text-white shadow-lg transition flex items-center justify-center gap-2 cursor-pointer ${
                role === 'OMB_HOST'
                  ? 'bg-indigo-600 hover:bg-indigo-500 shadow-indigo-600/25'
                  : 'bg-emerald-600 hover:bg-emerald-500 shadow-emerald-600/25'
              }`}
            >
              Sign In to Host Panel
              <ArrowRight className="w-4 h-4" />
            </button>
          </form>

          {/* Quick Switch Profiles */}
          <div className="pt-4 border-t border-slate-800 space-y-2.5">
            <div className="text-[11px] font-semibold text-slate-400 flex items-center gap-1">
              <KeyRound className="w-3.5 h-3.5 text-indigo-400" />
              Quick Test Profiles (Pre-configured seeds):
            </div>
            <div className="grid grid-cols-1 gap-2">
              <button
                type="button"
                onClick={() => handleQuickLogin('HOST_OMB_01', 'OMB_HOST')}
                className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-indigo-500/50 text-left flex items-center justify-between transition cursor-pointer group"
              >
                <div>
                  <div className="text-xs font-bold text-white group-hover:text-indigo-300">Rahul Sharma (OMB Host)</div>
                  <div className="text-[10px] text-slate-400 font-mono">ID: HOST_OMB_01 • Active</div>
                </div>
                <span className="text-[10px] bg-indigo-500/10 text-indigo-400 border border-indigo-500/20 px-2 py-0.5 rounded-md font-bold">
                  Select
                </span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('HOST_TOURN_01', 'TOURNAMENT_HOST')}
                className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-emerald-500/50 text-left flex items-center justify-between transition cursor-pointer group"
              >
                <div>
                  <div className="text-xs font-bold text-white group-hover:text-emerald-300">Vikram Patel (Tournament Host)</div>
                  <div className="text-[10px] text-slate-400 font-mono">ID: HOST_TOURN_01 • Active</div>
                </div>
                <span className="text-[10px] bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 px-2 py-0.5 rounded-md font-bold">
                  Select
                </span>
              </button>

              <button
                type="button"
                onClick={() => handleQuickLogin('HOST_DISABLED_01', 'OMB_HOST')}
                className="p-2.5 rounded-xl bg-slate-950/60 border border-slate-800 hover:border-red-500/50 text-left flex items-center justify-between transition cursor-pointer group"
              >
                <div>
                  <div className="text-xs font-bold text-white group-hover:text-red-300">Suresh Kumar (Suspended / Disabled)</div>
                  <div className="text-[10px] text-red-400 font-mono">ID: HOST_DISABLED_01 • Claim Disabled</div>
                </div>
                <span className="text-[10px] bg-red-500/10 text-red-400 border border-red-500/20 px-2 py-0.5 rounded-md font-bold">
                  Test Lock
                </span>
              </button>
            </div>
          </div>
        </div>

        {/* Security badge footer */}
        <div className="text-center text-[11px] text-slate-500">
          Double-verification active • EC2 backend synced • Single running assignment policy enforced
        </div>
      </div>
    </div>
  );
};

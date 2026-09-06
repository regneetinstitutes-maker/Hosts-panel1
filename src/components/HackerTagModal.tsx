import React, { useState } from 'react';
import { Skull, AlertOctagon, X, ArrowRight, ArrowLeft } from 'lucide-react';

interface HackerTagModalProps {
  participantName: string;
  gameId: string;
  onDismiss: () => void;
  onConfirmed: (reason: string) => void;
}

export const HackerTagModal: React.FC<HackerTagModalProps> = ({
  participantName,
  gameId,
  onDismiss,
  onConfirmed
}) => {
  const [step, setStep] = useState<1 | 2>(1);
  const [reason, setReason] = useState('In-game aimbot / speed hack detected');

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-xs">
      <div className="w-full max-w-md bg-slate-900 border border-red-900/60 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-red-950/40">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-red-500/20 border border-red-500/30 flex items-center justify-center text-red-400">
              <Skull className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-red-300">Tag as Hacker / Cheater</h3>
              <p className="text-xs text-red-400">
                {step === 1 ? 'Verification Step 1' : 'Final Confirmation Step 2'}
              </p>
            </div>
          </div>
          <button
            onClick={onDismiss}
            className="text-slate-400 hover:text-white p-1 rounded-lg hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        <div className="p-6 space-y-4">
          <div className="p-3.5 rounded-xl bg-red-950/30 border border-red-800/40 space-y-1.5">
            <div className="flex justify-between items-center">
              <span className="text-xs text-slate-400">Participant:</span>
              <span className="text-xs font-bold text-white">{participantName}</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-xs text-slate-400">Game ID:</span>
              <span className="text-xs font-mono text-indigo-300">{gameId}</span>
            </div>
            <div className="pt-2 border-t border-red-900/40 space-y-1 text-xs text-red-300">
              <p>• <strong>Prize = ₹0</strong> (Forfeited)</p>
              <p>• <strong>Refund = ₹0</strong> (Forfeited)</p>
              <p>• Tag is publicly visible to all participants and admin records.</p>
            </div>
          </div>

          {step === 1 ? (
            <div className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Violation Details / Reason:
                </label>
                <select
                  value={reason}
                  onChange={(e) => setReason(e.target.value)}
                  className="w-full px-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-xs focus:outline-hidden focus:border-red-500 transition"
                >
                  <option value="In-game aimbot / speed hack detected">In-game aimbot / speed hack detected</option>
                  <option value="Wallhack / ESP overlay confirmed">Wallhack / ESP overlay confirmed</option>
                  <option value="Teaming / Collusion in solo match">Teaming / Collusion in solo match</option>
                  <option value="Emulator / Third-party script bypassing mobile lobby">Emulator / Third-party script bypassing mobile lobby</option>
                  <option value="Other gross violation">Other gross violation</option>
                </select>
              </div>

              <p className="text-xs text-slate-400">
                Are you sure you want to flag this player as a Hacker/Cheater? This requires a secondary confirmation step.
              </p>

              <div className="flex items-center justify-end gap-2.5 pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={onDismiss}
                  className="px-4 py-2 text-xs font-medium text-slate-400 hover:text-white transition"
                >
                  Cancel
                </button>
                <button
                  type="button"
                  onClick={() => setStep(2)}
                  className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-semibold flex items-center gap-1.5 shadow-md shadow-red-600/20 transition cursor-pointer"
                >
                  Proceed to Step 2 <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </div>
          ) : (
            <div className="space-y-4">
              <div className="p-3 bg-red-950/50 border border-red-700/60 rounded-xl text-xs text-red-200 flex items-start gap-2.5">
                <AlertOctagon className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
                <span>
                  <strong>FINAL CONFIRMATION:</strong> Double-entry verification step. Click &apos;Confirm Tag&apos; to execute this irreversible penalty.
                </span>
              </div>

              <div className="flex items-center justify-between pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => setStep(1)}
                  className="px-3 py-2 text-xs font-medium text-slate-400 hover:text-white flex items-center gap-1 transition"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back
                </button>
                <button
                  type="button"
                  onClick={() => onConfirmed(reason)}
                  className="px-4 py-2 bg-red-600 hover:bg-red-500 text-white rounded-xl text-xs font-semibold shadow-md shadow-red-600/30 transition cursor-pointer"
                >
                  Confirm Tag as Hacker
                </button>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

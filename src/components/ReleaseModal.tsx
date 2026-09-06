import React, { useState } from 'react';
import { AlertTriangle, X } from 'lucide-react';

interface ReleaseModalProps {
  assignmentId: string;
  onDismiss: () => void;
  onConfirmRelease: (text: string) => void;
}

export const ReleaseModal: React.FC<ReleaseModalProps> = ({
  assignmentId,
  onDismiss,
  onConfirmRelease
}) => {
  const [typedText, setTypedText] = useState('');
  const [errorText, setErrorText] = useState<string | null>(null);

  const isExactMatch = typedText.trim() === 'release';

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!isExactMatch) {
      setErrorText("You must type 'release' exactly to confirm.");
      return;
    }
    onConfirmRelease(typedText.trim());
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-xs">
      <div className="w-full max-w-md bg-slate-900 border border-red-900/40 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-red-950/20">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-red-500/10 border border-red-500/20 flex items-center justify-center text-red-400">
              <AlertTriangle className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white">Release Assignment #{assignmentId}</h3>
              <p className="text-xs text-red-400">Confirmation Required</p>
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
          <p className="text-xs text-slate-300 leading-relaxed">
            Releasing this match will return it to the Available pool for another host to claim. Your active assignment status will be cleared.
          </p>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Type <span className="font-mono text-amber-400 font-bold">release</span> to confirm:
            </label>
            <input
              type="text"
              value={typedText}
              onChange={(e) => {
                setTypedText(e.target.value);
                setErrorText(null);
              }}
              autoFocus
              placeholder="release"
              className="w-full px-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-sm focus:outline-hidden focus:border-red-500 transition font-mono"
            />
            {errorText && (
              <p className="text-xs text-red-400 mt-1.5">{errorText}</p>
            )}
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
              disabled={!isExactMatch}
              className={`px-4 py-2 rounded-xl text-xs font-semibold shadow-md transition cursor-pointer ${
                isExactMatch
                  ? 'bg-red-600 hover:bg-red-500 text-white shadow-red-600/20'
                  : 'bg-slate-800 text-slate-500 cursor-not-allowed'
              }`}
            >
              Confirm Release
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

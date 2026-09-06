import React, { useState } from 'react';
import { ShieldCheck, AlertCircle, ArrowRight, ArrowLeft, X } from 'lucide-react';

interface DoubleVerificationModalProps {
  title: string;
  primaryFieldLabel: string;
  secondaryFieldLabel?: string;
  isNumeric?: boolean;
  isSecret?: boolean;
  onDismiss: () => void;
  onConfirmed: (val1: string, val2: string) => void;
}

export const DoubleVerificationModal: React.FC<DoubleVerificationModalProps> = ({
  title,
  primaryFieldLabel,
  secondaryFieldLabel,
  isNumeric = false,
  isSecret = false,
  onDismiss,
  onConfirmed
}) => {
  const [step, setStep] = useState<1 | 2>(1);
  const [firstField1, setFirstField1] = useState('');
  const [firstField2, setFirstField2] = useState('');
  const [secondField1, setSecondField1] = useState('');
  const [secondField2, setSecondField2] = useState('');
  const [localError, setLocalError] = useState<string | null>(null);

  const handleStep1Submit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!firstField1.trim() || (secondaryFieldLabel && !firstField2.trim())) {
      setLocalError('Please fill in all required fields.');
      return;
    }
    setLocalError(null);
    setStep(2);
  };

  const handleStep2Submit = (e: React.FormEvent) => {
    e.preventDefault();
    const match1 = firstField1.trim() === secondField1.trim();
    const match2 = !secondaryFieldLabel || firstField2.trim() === secondField2.trim();

    if (!match1 || !match2) {
      setLocalError("You haven't entered information correctly. Please re-enter carefully and with concentration.");
      return;
    }

    onConfirmed(firstField1.trim(), firstField2.trim());
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/70 backdrop-blur-xs">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        {/* Header */}
        <div className="p-5 border-b border-slate-800 flex items-start justify-between bg-slate-900/60">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
              <ShieldCheck className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white">{title}</h3>
              <p className="text-xs font-medium text-indigo-400">
                {step === 1 ? 'Step 1 of 2: Enter Details' : 'Step 2 of 2: Re-enter for Verification'}
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

        {/* Content */}
        <div className="p-6">
          {localError && (
            <div className="mb-4 p-3 rounded-xl bg-red-950/40 border border-red-800/60 flex items-start gap-2.5 text-red-300 text-xs">
              <AlertCircle className="w-4 h-4 text-red-400 shrink-0 mt-0.5" />
              <span>{localError}</span>
            </div>
          )}

          {step === 1 ? (
            <form onSubmit={handleStep1Submit} className="space-y-4">
              <p className="text-xs text-slate-400">
                Please enter the information carefully and accurately.
              </p>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  {primaryFieldLabel}
                </label>
                <input
                  type={isNumeric ? 'number' : 'text'}
                  value={firstField1}
                  onChange={(e) => {
                    setFirstField1(e.target.value);
                    setLocalError(null);
                  }}
                  autoFocus
                  placeholder={`Enter ${primaryFieldLabel}`}
                  className="w-full px-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-sm focus:outline-hidden focus:border-indigo-500 transition"
                />
              </div>

              {secondaryFieldLabel && (
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    {secondaryFieldLabel}
                  </label>
                  <input
                    type={isSecret ? 'password' : isNumeric ? 'number' : 'text'}
                    value={firstField2}
                    onChange={(e) => {
                      setFirstField2(e.target.value);
                      setLocalError(null);
                    }}
                    placeholder={`Enter ${secondaryFieldLabel}`}
                    className="w-full px-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-sm focus:outline-hidden focus:border-indigo-500 transition"
                  />
                </div>
              )}

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
                  className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold flex items-center gap-1.5 shadow-md shadow-indigo-600/20 transition cursor-pointer"
                >
                  Confirm <ArrowRight className="w-3.5 h-3.5" />
                </button>
              </div>
            </form>
          ) : (
            <form onSubmit={handleStep2Submit} className="space-y-4">
              <p className="text-xs text-slate-400">
                Re-enter the details below to verify matching values.
              </p>

              <div>
                <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                  Re-enter {primaryFieldLabel}
                </label>
                <input
                  type={isNumeric ? 'number' : 'text'}
                  value={secondField1}
                  onChange={(e) => {
                    setSecondField1(e.target.value);
                    setLocalError(null);
                  }}
                  autoFocus
                  placeholder={`Re-enter ${primaryFieldLabel}`}
                  className="w-full px-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-sm focus:outline-hidden focus:border-indigo-500 transition"
                />
              </div>

              {secondaryFieldLabel && (
                <div>
                  <label className="block text-xs font-semibold text-slate-300 mb-1.5">
                    Re-enter {secondaryFieldLabel}
                  </label>
                  <input
                    type={isSecret ? 'password' : isNumeric ? 'number' : 'text'}
                    value={secondField2}
                    onChange={(e) => {
                      setSecondField2(e.target.value);
                      setLocalError(null);
                    }}
                    placeholder={`Re-enter ${secondaryFieldLabel}`}
                    className="w-full px-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-sm focus:outline-hidden focus:border-indigo-500 transition"
                  />
                </div>
              )}

              <div className="flex items-center justify-between pt-4 border-t border-slate-800">
                <button
                  type="button"
                  onClick={() => {
                    setStep(1);
                    setSecondField1('');
                    setSecondField2('');
                    setLocalError(null);
                  }}
                  className="px-3 py-2 text-xs font-medium text-slate-400 hover:text-white flex items-center gap-1 transition"
                >
                  <ArrowLeft className="w-3.5 h-3.5" /> Back
                </button>
                <button
                  type="submit"
                  className="px-4 py-2 bg-emerald-600 hover:bg-emerald-500 text-white rounded-xl text-xs font-semibold shadow-md shadow-emerald-600/20 transition cursor-pointer"
                >
                  Confirm & Submit
                </button>
              </div>
            </form>
          )}
        </div>
      </div>
    </div>
  );
};

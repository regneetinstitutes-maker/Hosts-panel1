import React, { useState } from 'react';
import { Camera, CloudUpload, X, CheckCircle2 } from 'lucide-react';

interface ScreenshotUploadModalProps {
  matchId: string;
  currentScreenshotUrl?: string | null;
  onDismiss: () => void;
  onUploadScreenshot: (url: string) => void;
}

export const ScreenshotUploadModal: React.FC<ScreenshotUploadModalProps> = ({
  matchId,
  currentScreenshotUrl,
  onDismiss,
  onUploadScreenshot
}) => {
  const presetImages = [
    { file: 'bgmi_erangle_winner_match_result.png', label: 'BGMI Winner Scoreboard (Official)' },
    { file: 'freefire_booyah_squad_result.png', label: 'Free Fire Squad Standings Screen' },
    { file: 'custom_esports_result_table.png', label: 'Custom Match End Overview' }
  ];

  const [selectedOption, setSelectedOption] = useState(
    currentScreenshotUrl || presetImages[0].file
  );
  const [customUrl, setCustomUrl] = useState('');
  const [isCustomInput, setIsCustomInput] = useState(false);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    const finalUrl = isCustomInput && customUrl.trim() ? customUrl.trim() : selectedOption;
    onUploadScreenshot(finalUrl);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/75 backdrop-blur-xs">
      <div className="w-full max-w-md bg-slate-900 border border-slate-800 rounded-2xl shadow-2xl overflow-hidden animate-in fade-in zoom-in duration-200">
        <div className="p-5 border-b border-slate-800 flex items-center justify-between bg-slate-900/60">
          <div className="flex items-center gap-3">
            <div className="w-10 h-10 rounded-xl bg-indigo-500/10 border border-indigo-500/20 flex items-center justify-center text-indigo-400">
              <Camera className="w-5 h-5" />
            </div>
            <div>
              <h3 className="text-base font-semibold text-white">Match Result Screenshot</h3>
              <p className="text-xs text-indigo-400">Associate with #{matchId}</p>
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
          <p className="text-xs text-slate-300">
            Upload the overall in-game match result scoreboard for verification before submitting results and issuing payouts.
          </p>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-2">
              Select In-Game Capture Preset:
            </label>
            <div className="space-y-2">
              {presetImages.map((preset) => {
                const isSelected = !isCustomInput && selectedOption === preset.file;
                return (
                  <div
                    key={preset.file}
                    onClick={() => {
                      setSelectedOption(preset.file);
                      setIsCustomInput(false);
                    }}
                    className={`p-3 rounded-xl border transition cursor-pointer flex items-center justify-between ${
                      isSelected
                        ? 'bg-indigo-950/40 border-indigo-500/50 text-white'
                        : 'bg-slate-950/40 border-slate-800 text-slate-300 hover:border-slate-700'
                    }`}
                  >
                    <div>
                      <div className="text-xs font-semibold">{preset.label}</div>
                      <div className="text-[11px] text-slate-400 font-mono mt-0.5">{preset.file}</div>
                    </div>
                    {isSelected && <CheckCircle2 className="w-4 h-4 text-indigo-400 shrink-0" />}
                  </div>
                );
              })}
            </div>
          </div>

          <div>
            <label className="block text-xs font-semibold text-slate-300 mb-1.5">
              Or Enter Custom Storage URL:
            </label>
            <input
              type="text"
              value={customUrl}
              onChange={(e) => {
                setCustomUrl(e.target.value);
                if (e.target.value.trim()) setIsCustomInput(true);
              }}
              placeholder="https://s3.aws.amazon.com/match-results/..."
              className="w-full px-3.5 py-2.5 bg-slate-950/80 border border-slate-700 rounded-xl text-white text-xs focus:outline-hidden focus:border-indigo-500 transition font-mono"
            />
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
              className="px-4 py-2 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-xs font-semibold flex items-center gap-1.5 shadow-md shadow-indigo-600/20 transition cursor-pointer"
            >
              <CloudUpload className="w-4 h-4" />
              Upload Screenshot
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

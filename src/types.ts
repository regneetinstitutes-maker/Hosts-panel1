export type HostRole = 'OMB_HOST' | 'TOURNAMENT_HOST';

export type HostStatus = 'ACTIVE' | 'DISABLED';

export interface HostUser {
  id: string;
  username: string;
  fullName: string;
  role: HostRole;
  status: HostStatus;
  currentAssignmentId?: string | null;
  currentAssignmentType?: 'OMB' | 'TOURNAMENT' | null;
}

export type OmbMatchStatus = 'AVAILABLE' | 'RUNNING' | 'CANCELLED' | 'COMPLETED';

export interface OmbParticipant {
  id: string;
  name: string;
  gameIdName: string;
  inGameUid: string;
  slotNumber: number;
  teamNumber: number;
  kills?: number | null;
  isHackerCheater: boolean;
  hackerReason?: string | null;
  refundProcessed: boolean;
}

export interface OmbMatch {
  id: string;
  game: string;
  mode: string;
  entryFee: number;
  perKillPrize: number;
  roomId?: string | null;
  roomPassword?: string | null;
  status: OmbMatchStatus;
  slotNumbers: number[];
  participantsJoined: number;
  maxParticipants: number;
  claimedByHostId?: string | null;
  screenshotUrl?: string | null;
  participants: OmbParticipant[];
}

export type TournamentStatus = 'AVAILABLE' | 'RUNNING' | 'CANCELLED' | 'COMPLETED';

export interface TournamentPrize {
  rank: number;
  prize: number;
}

export interface ScheduleStanding {
  position: number;
  gameId: string;
  metricValue: number;
  atTime: string;
}

export interface TournamentSchedule {
  id: string;
  name: string;
  time: string;
  isSubmitted: boolean;
  standings: ScheduleStanding[];
}

export interface TournamentParticipant {
  id: string;
  name: string;
  gameIdName: string;
  inGameUid: string;
  initialValue?: number | null;
  finalValue?: number | null;
  performance?: number | null;
  rank?: number | null;
  isHackerCheater: boolean;
  hackerReason?: string | null;
}

export interface Tournament {
  id: string;
  game: string;
  mode: string;
  entryFee: number;
  durationMinutes: number;
  resultsOn: string;
  tournamentMetric: string;
  teamSize: string;
  status: TournamentStatus;
  participantsJoined: number;
  maxParticipants: number;
  claimedByHostId?: string | null;
  prizeChart: TournamentPrize[];
  participants: TournamentParticipant[];
  schedules: TournamentSchedule[];
}

export type AlertSeverity = 'CRITICAL' | 'WARNING' | 'INFO';

export interface OperationalAlert {
  id: string;
  severity: AlertSeverity;
  message: string;
  timestamp: number;
  matchOrTournamentId: string;
  matchDetails: string;
}

export interface HostNotification {
  id: string;
  title: string;
  message: string;
  matchId: string;
  type: 'OMB' | 'TOURNAMENT';
  game: string;
  mode: string;
  entryFee: number;
  timestamp: number;
}

export type HostTab = 'AVAILABLE' | 'RUNNING';

export interface HostUiState {
  currentHost: HostUser | null;
  availableOmbs: OmbMatch[];
  runningOmb: OmbMatch | null;
  availableTournaments: Tournament[];
  runningTournament: Tournament | null;
  operationalAlerts: OperationalAlert[];
  notifications: HostNotification[];
  selectedTab: HostTab;
  isLoading: boolean;
  loadingMessage: string;
  errorMessage: string | null;
  successMessage: string | null;
}

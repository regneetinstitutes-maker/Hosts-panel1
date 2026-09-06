import {
  HostUser,
  OmbMatch,
  Tournament,
  OperationalAlert,
  HostNotification,
  HostRole
} from '../types';

const STORAGE_KEY_HOST = 'host_panel_current_user';
const STORAGE_KEY_OMBS = 'host_panel_ombs';
const STORAGE_KEY_TOURNAMENTS = 'host_panel_tournaments';
const STORAGE_KEY_ALERTS = 'host_panel_alerts';
const STORAGE_KEY_NOTIFS = 'host_panel_notifs';

// Initial Seeds
const DEFAULT_HOSTS: HostUser[] = [
  {
    id: 'HOST_OMB_01',
    username: 'rahul_omb',
    fullName: 'Rahul Sharma',
    role: 'OMB_HOST',
    status: 'ACTIVE',
    currentAssignmentId: null,
    currentAssignmentType: null
  },
  {
    id: 'HOST_TOURN_01',
    username: 'vikram_tourn',
    fullName: 'Vikram Patel',
    role: 'TOURNAMENT_HOST',
    status: 'ACTIVE',
    currentAssignmentId: null,
    currentAssignmentType: null
  },
  {
    id: 'HOST_DISABLED_01',
    username: 'suresh_disabled',
    fullName: 'Suresh Kumar (Suspended)',
    role: 'OMB_HOST',
    status: 'DISABLED',
    currentAssignmentId: null,
    currentAssignmentType: null
  }
];

const DEFAULT_OMBS: OmbMatch[] = [
  {
    id: 'OMB_8841',
    game: 'BGMI',
    mode: 'Solo • Erangel',
    entryFee: 50,
    perKillPrize: 30,
    roomId: null,
    roomPassword: null,
    status: 'AVAILABLE',
    slotNumbers: [1, 2, 3, 4, 5, 6, 7, 8, 9, 10],
    participantsJoined: 8,
    maxParticipants: 10,
    claimedByHostId: null,
    screenshotUrl: null,
    participants: [
      { id: 'p1', name: 'Aman Varma', gameIdName: 'DestroGod', inGameUid: '5129384711', slotNumber: 1, teamNumber: 1, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p2', name: 'Rohit Negi', gameIdName: 'HydraViper', inGameUid: '5129384712', slotNumber: 2, teamNumber: 2, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p3', name: 'Ankit Rawat', gameIdName: 'SoulMortalFan', inGameUid: '5129384713', slotNumber: 3, teamNumber: 3, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p4', name: 'Deepak Joshi', gameIdName: 'ClutchGod99', inGameUid: '5129384714', slotNumber: 4, teamNumber: 4, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p5', name: 'Karan Mehra', gameIdName: 'SniperKing', inGameUid: '5129384715', slotNumber: 5, teamNumber: 5, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p6', name: 'Sameer Sen', gameIdName: 'ShadowNinja', inGameUid: '5129384716', slotNumber: 6, teamNumber: 6, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p7', name: 'Pooja Roy', gameIdName: 'QueenValkyrie', inGameUid: '5129384717', slotNumber: 7, teamNumber: 7, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p8', name: 'Tarun Saxena', gameIdName: 'BeastMaster', inGameUid: '5129384718', slotNumber: 8, teamNumber: 8, kills: null, isHackerCheater: false, refundProcessed: false }
    ]
  },
  {
    id: 'OMB_8842',
    game: 'Free Fire MAX',
    mode: 'Squad • Bermuda',
    entryFee: 100,
    perKillPrize: 60,
    roomId: null,
    roomPassword: null,
    status: 'AVAILABLE',
    slotNumbers: [1, 2, 3, 4],
    participantsJoined: 4,
    maxParticipants: 4,
    claimedByHostId: null,
    screenshotUrl: null,
    participants: [
      { id: 'p21', name: 'Team Alpha', gameIdName: 'AlphaLeader', inGameUid: '6219384701', slotNumber: 1, teamNumber: 1, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p22', name: 'Team Delta', gameIdName: 'DeltaLeader', inGameUid: '6219384702', slotNumber: 2, teamNumber: 2, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p23', name: 'Team Sigma', gameIdName: 'SigmaLeader', inGameUid: '6219384703', slotNumber: 3, teamNumber: 3, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p24', name: 'Team Omega', gameIdName: 'OmegaLeader', inGameUid: '6219384704', slotNumber: 4, teamNumber: 4, kills: null, isHackerCheater: false, refundProcessed: false }
    ]
  },
  {
    id: 'OMB_8840',
    game: 'BGMI',
    mode: 'Duo • Miramar',
    entryFee: 40,
    perKillPrize: 25,
    roomId: '8840192',
    roomPassword: 'bgmi@pass12',
    status: 'AVAILABLE',
    slotNumbers: [1, 2, 3, 4, 5, 6],
    participantsJoined: 6,
    maxParticipants: 6,
    claimedByHostId: null,
    screenshotUrl: null,
    participants: [
      { id: 'p31', name: 'Duo Titans', gameIdName: 'TitanX', inGameUid: '7319384701', slotNumber: 1, teamNumber: 1, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p32', name: 'Duo Phoenix', gameIdName: 'PhoenixY', inGameUid: '7319384702', slotNumber: 2, teamNumber: 2, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p33', name: 'Duo Raptors', gameIdName: 'RaptorZ', inGameUid: '7319384703', slotNumber: 3, teamNumber: 3, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p34', name: 'Duo Blitz', gameIdName: 'BlitzW', inGameUid: '7319384704', slotNumber: 4, teamNumber: 4, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p35', name: 'Duo Apex', gameIdName: 'ApexA', inGameUid: '7319384705', slotNumber: 5, teamNumber: 5, kills: null, isHackerCheater: false, refundProcessed: false },
      { id: 'p36', name: 'Duo Vortex', gameIdName: 'VortexB', inGameUid: '7319384706', slotNumber: 6, teamNumber: 6, kills: null, isHackerCheater: false, refundProcessed: false }
    ]
  }
];

const DEFAULT_TOURNAMENTS: Tournament[] = [
  {
    id: 'TRN_9011',
    game: 'BGMI Premier League',
    mode: 'Squad • 4 Matches',
    entryFee: 250,
    durationMinutes: 120,
    resultsOn: 'Today, 10:30 PM',
    tournamentMetric: 'Placement & Kill Points',
    teamSize: 'Squad (4v4)',
    status: 'AVAILABLE',
    participantsJoined: 8,
    maxParticipants: 8,
    claimedByHostId: null,
    prizeChart: [
      { rank: 1, prize: 8000 },
      { rank: 2, prize: 4000 },
      { rank: 3, prize: 2000 }
    ],
    participants: [
      { id: 'tp1', name: 'Soul Army', gameIdName: 'Soul_Akash', inGameUid: '8910283711', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp2', name: 'GodLike Esports', gameIdName: 'GodL_Jonathan', inGameUid: '8910283712', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp3', name: 'Blind Esports', gameIdName: 'Blind_Manya', inGameUid: '8910283713', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp4', name: 'Gladiators Esports', gameIdName: 'Glad_Destro', inGameUid: '8910283714', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp5', name: 'Team XSpark', gameIdName: 'TX_ScoutOP', inGameUid: '8910283715', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp6', name: 'Revenant Esports', gameIdName: 'RNT_Sensei', inGameUid: '8910283716', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp7', name: 'Orangutan Gaming', gameIdName: 'OG_WizzGOD', inGameUid: '8910283717', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp8', name: 'Entity Gaming', gameIdName: 'Entity_Saumraj', inGameUid: '8910283718', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false }
    ],
    schedules: [
      {
        id: 'sch_1',
        name: 'Match 1 (Erangel) Standings',
        time: '8:00 PM',
        isSubmitted: false,
        standings: []
      },
      {
        id: 'sch_2',
        name: 'Match 2 (Miramar) Standings',
        time: '8:45 PM',
        isSubmitted: false,
        standings: []
      },
      {
        id: 'sch_3',
        name: 'Match 3 (Sanhok) Standings',
        time: '9:30 PM',
        isSubmitted: false,
        standings: []
      },
      {
        id: 'sch_4',
        name: 'Grand Final Match 4 Standings',
        time: '10:15 PM',
        isSubmitted: false,
        standings: []
      }
    ]
  },
  {
    id: 'TRN_9012',
    game: 'Free Fire Masters Cup',
    mode: 'Solo • Clash Squad',
    entryFee: 150,
    durationMinutes: 90,
    resultsOn: 'Today, 9:00 PM',
    tournamentMetric: 'Eliminations',
    teamSize: 'Solo',
    status: 'AVAILABLE',
    participantsJoined: 6,
    maxParticipants: 6,
    claimedByHostId: null,
    prizeChart: [
      { rank: 1, prize: 3500 },
      { rank: 2, prize: 1500 }
    ],
    participants: [
      { id: 'tp21', name: 'Pahadi Gamer', gameIdName: 'Pahadi_FF', inGameUid: '9021384701', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp22', name: 'Killer FF', gameIdName: 'Killer_77', inGameUid: '9021384702', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp23', name: 'Vasiyo CRJ7', gameIdName: 'Vasiyo_God', inGameUid: '9021384703', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp24', name: 'Fozy Ajay', gameIdName: 'FozyAjay', inGameUid: '9021384704', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp25', name: 'Mafia Bala', gameIdName: 'Bala_FF', inGameUid: '9021384705', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false },
      { id: 'tp26', name: 'Nawab Gaming', gameIdName: 'Nawab_OP', inGameUid: '9021384706', initialValue: null, finalValue: null, performance: null, rank: null, isHackerCheater: false }
    ],
    schedules: [
      {
        id: 'sch_ff1',
        name: 'Round of 16 Snapshot',
        time: '7:45 PM',
        isSubmitted: false,
        standings: []
      },
      {
        id: 'sch_ff2',
        name: 'Semi-Final Snapshot',
        time: '8:30 PM',
        isSubmitted: false,
        standings: []
      }
    ]
  }
];

const DEFAULT_ALERTS: OperationalAlert[] = [
  {
    id: 'alt_1',
    severity: 'WARNING',
    message: 'Active Match Rule: You cannot claim another assignment while a match or tournament is running.',
    timestamp: Date.now() - 1000 * 60 * 15,
    matchOrTournamentId: 'SYSTEM',
    matchDetails: 'Host Policy Compliance'
  },
  {
    id: 'alt_2',
    severity: 'INFO',
    message: 'Double verification is mandated for Room Credentials, Kills, and Performance Metric Values.',
    timestamp: Date.now() - 1000 * 60 * 30,
    matchOrTournamentId: 'POLICY',
    matchDetails: 'Verification Guideline'
  }
];

const DEFAULT_NOTIFS: HostNotification[] = [
  {
    id: 'notif_1',
    title: 'High-Demand OMB Match Scheduled',
    message: 'BGMI Solo Erangel is filled and awaiting host room allocation.',
    matchId: 'OMB_8841',
    type: 'OMB',
    game: 'BGMI',
    mode: 'Solo • Erangel',
    entryFee: 50,
    timestamp: Date.now() - 1000 * 60 * 5
  },
  {
    id: 'notif_2',
    title: 'Esports Tournament Needs Host',
    message: 'BGMI Premier League is ready for Host Claiming and initial verification.',
    matchId: 'TRN_9011',
    type: 'TOURNAMENT',
    game: 'BGMI Premier League',
    mode: 'Squad • 4 Matches',
    entryFee: 250,
    timestamp: Date.now() - 1000 * 60 * 12
  }
];

export class HostStorageService {
  static getStoredHost(): HostUser | null {
    const raw = localStorage.getItem(STORAGE_KEY_HOST);
    if (!raw) return null;
    try {
      return JSON.parse(raw);
    } catch {
      return null;
    }
  }

  static saveStoredHost(host: HostUser | null): void {
    if (!host) {
      localStorage.removeItem(STORAGE_KEY_HOST);
    } else {
      localStorage.setItem(STORAGE_KEY_HOST, JSON.stringify(host));
    }
  }

  static getOmbs(): OmbMatch[] {
    const raw = localStorage.getItem(STORAGE_KEY_OMBS);
    if (!raw) {
      this.saveOmbs(DEFAULT_OMBS);
      return DEFAULT_OMBS;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return DEFAULT_OMBS;
    }
  }

  static saveOmbs(ombs: OmbMatch[]): void {
    localStorage.setItem(STORAGE_KEY_OMBS, JSON.stringify(ombs));
  }

  static getTournaments(): Tournament[] {
    const raw = localStorage.getItem(STORAGE_KEY_TOURNAMENTS);
    if (!raw) {
      this.saveTournaments(DEFAULT_TOURNAMENTS);
      return DEFAULT_TOURNAMENTS;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return DEFAULT_TOURNAMENTS;
    }
  }

  static saveTournaments(tournaments: Tournament[]): void {
    localStorage.setItem(STORAGE_KEY_TOURNAMENTS, JSON.stringify(tournaments));
  }

  static getAlerts(): OperationalAlert[] {
    const raw = localStorage.getItem(STORAGE_KEY_ALERTS);
    if (!raw) {
      this.saveAlerts(DEFAULT_ALERTS);
      return DEFAULT_ALERTS;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return DEFAULT_ALERTS;
    }
  }

  static saveAlerts(alerts: OperationalAlert[]): void {
    localStorage.setItem(STORAGE_KEY_ALERTS, JSON.stringify(alerts));
  }

  static getNotifications(): HostNotification[] {
    const raw = localStorage.getItem(STORAGE_KEY_NOTIFS);
    if (!raw) {
      this.saveNotifications(DEFAULT_NOTIFS);
      return DEFAULT_NOTIFS;
    }
    try {
      return JSON.parse(raw);
    } catch {
      return DEFAULT_NOTIFS;
    }
  }

  static saveNotifications(notifs: HostNotification[]): void {
    localStorage.setItem(STORAGE_KEY_NOTIFS, JSON.stringify(notifs));
  }

  static getDemoHosts(): HostUser[] {
    return DEFAULT_HOSTS;
  }

  static resetToDefault(): void {
    localStorage.removeItem(STORAGE_KEY_HOST);
    localStorage.setItem(STORAGE_KEY_OMBS, JSON.stringify(DEFAULT_OMBS));
    localStorage.setItem(STORAGE_KEY_TOURNAMENTS, JSON.stringify(DEFAULT_TOURNAMENTS));
    localStorage.setItem(STORAGE_KEY_ALERTS, JSON.stringify(DEFAULT_ALERTS));
    localStorage.setItem(STORAGE_KEY_NOTIFS, JSON.stringify(DEFAULT_NOTIFS));
  }
}

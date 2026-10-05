import {
  Activity,
  Archive,
  Award,
  BarChart,
  BarChart3,
  Bell,
  BookCheck,
  BookmarkCheck,
  BookOpen,
  Boxes,
  Brain,
  Bug,
  Building2,
  Calendar,
  ChevronRight,
  Clock,
  Cog,
  CreditCard,
  Database,
  DollarSign,
  FileCheck,
  FileImage,
  FilePlus,
  FileText,
  Globe,
  GraduationCap,
  Grid3X3,
  Headset,
  History,
  IdCard,
  Languages,
  LayoutDashboard,
  LibraryBig,
  List,
  ListChecks,
  ListTree,
  Mail,
  MailOpen,
  Megaphone,
  MenuIcon,
  MessagesSquare,
  Newspaper,
  Package,
  PackageOpen,
  Palette,
  Percent,
  PhoneCall,
  Plus,
  PlusCircle,
  PlusSquare,
  RefreshCw,
  ScrollText,
  Send,
  Settings,
  Shield,
  ShieldCheck,
  Sparkles,
  SquareLibrary,
  Tag,
  Tags,
  TicketPercent,
  TrendingUp,
  Trophy,
  Upload,
  UserCheck,
  UserCircle,
  UserMinus,
  UserPlus,
  Users,
  UserX,
  Video,
  Wallet,
} from 'lucide-react';

export const SidebarMenuIcons = [
  { label: 'Activity', value: Activity },
  { label: 'Archive', value: Archive },
  { label: 'Award', value: Award },
  { label: 'BarChart', value: BarChart },
  { label: 'BarChart3', value: BarChart3 },
  { label: 'Bell', value: Bell },
  { label: 'BookCheck', value: BookCheck },
  { label: 'BookmarkCheck', value: BookmarkCheck },
  { label: 'BookOpen', value: BookOpen },
  { label: 'Boxes', value: Boxes },
  { label: 'Brain', value: Brain },
  { label: 'Bug', value: Bug },
  { label: 'Building2', value: Building2 },
  { label: 'Calendar', value: Calendar },
  { label: 'ChevronRight', value: ChevronRight },
  { label: 'Clock', value: Clock },
  { label: 'Cog', value: Cog },
  { label: 'CreditCard', value: CreditCard },
  { label: 'Database', value: Database },
  { label: 'DollarSign', value: DollarSign },
  { label: 'FileCheck', value: FileCheck },
  { label: 'FileImage', value: FileImage },
  { label: 'FilePlus', value: FilePlus },
  { label: 'FileText', value: FileText },
  { label: 'GraduationCap', value: GraduationCap },
  { label: 'Globe', value: Globe },
  { label: 'Grid3X3', value: Grid3X3 },
  { label: 'Headset', value: Headset },
  { label: 'History', value: History },
  { label: 'IdCard', value: IdCard },
  { label: 'Language', value: Languages },
  { label: 'LayoutDashboard', value: LayoutDashboard },
  { label: 'LibraryBig', value: LibraryBig },
  { label: 'List', value: List },
  { label: 'ListChecks', value: ListChecks },
  { label: 'ListTree', value: ListTree },
  { label: 'Mail', value: Mail },
  { label: 'MailOpen', value: MailOpen },
  { label: 'Megaphone', value: Megaphone },
  { label: 'MenuIcon', value: MenuIcon },
  { label: 'MessagesSquare', value: MessagesSquare },
  { label: 'Newspaper', value: Newspaper },
  { label: 'Package', value: Package },
  { label: 'PackageOpen', value: PackageOpen },
  { label: 'Palette', value: Palette },
  { label: 'Percent', value: Percent },
  { label: 'PhoneCall', value: PhoneCall },
  { label: 'Plus', value: Plus },
  { label: 'PlusCircle', value: PlusCircle },
  { label: 'PlusSquare', value: PlusSquare },
  { label: 'RefreshCw', value: RefreshCw },
  { label: 'ScrollText', value: ScrollText },
  { label: 'Send', value: Send },
  { label: 'Settings', value: Settings },
  { label: 'Shield', value: Shield },
  { label: 'ShieldCheck', value: ShieldCheck },
  { label: 'Sparkles', value: Sparkles },
  { label: 'SquareLibrary', value: SquareLibrary },
  { label: 'Tag', value: Tag },
  { label: 'Tags', value: Tags },
  { label: 'TicketPercent', value: TicketPercent },
  { label: 'TrendingUp', value: TrendingUp },
  { label: 'Trophy', value: Trophy },
  { label: 'Upload', value: Upload },
  { label: 'UserCheck', value: UserCheck },
  { label: 'UserCircle', value: UserCircle },
  { label: 'UserMinus', value: UserMinus },
  { label: 'UserPlus', value: UserPlus },
  { label: 'Users', value: Users },
  { label: 'UserX', value: UserX },
  { label: 'Video', value: Video },
  { label: 'Wallet', value: Wallet },
];

export const GetMenuIcon = (icon: string) => {
  switch (icon) {
    case 'Activity':
      return <Activity className="size-4 text-primary" />;
    case 'Archive':
      return <Archive className="size-4 text-primary" />;
    case 'Award':
      return <Award className="size-4 text-primary" />;
    case 'BarChart':
      return <BarChart className="size-4 text-primary" />;
    case 'BarChart3':
      return <BarChart3 className="size-4 text-primary" />;
    case 'Bell':
      return <Bell className="size-4 text-primary" />;
    case 'BookCheck':
      return <BookCheck className="size-4 text-primary" />;
    case 'BookmarkCheck':
      return <BookmarkCheck className="size-4 text-primary" />;
    case 'BookOpen':
      return <BookOpen className="size-4 text-primary" />;
    case 'Boxes':
      return <Boxes className="size-4 text-primary" />;
    case 'Brain':
      return <Brain className="size-4 text-primary" />;
    case 'Bug':
      return <Bug className="size-4 text-primary" />;
    case 'Building2':
      return <Building2 className="size-4 text-primary" />;
    case 'Calendar':
      return <Calendar className="size-4 text-primary" />;
    case 'ChevronRight':
      return <ChevronRight className="size-4 text-primary" />;
    case 'Clock':
      return <Clock className="size-4 text-primary" />;
    case 'Cog':
      return <Cog className="size-4 text-primary" />;
    case 'CreditCard':
      return <CreditCard className="size-4 text-primary" />;
    case 'Database':
      return <Database className="size-4 text-primary" />;
    case 'DollarSign':
      return <DollarSign className="size-4 text-primary" />;
    case 'FileCheck':
      return <FileCheck className="size-4 text-primary" />;
    case 'FileImage':
      return <FileImage className="size-4 text-primary" />;
    case 'FilePlus':
      return <FilePlus className="size-4 text-primary" />;
    case 'FileText':
      return <FileText className="size-4 text-primary" />;
    case 'GraduationCap':
      return <GraduationCap className="size-4 text-primary" />;
    case 'Globe':
      return <Globe className="size-4 text-primary" />;
    case 'Grid3X3':
      return <Grid3X3 className="size-4 text-primary" />;
    case 'Headset':
      return <Headset className="size-4 text-primary" />;
    case 'History':
      return <History className="size-4 text-primary" />;
    case 'IdCard':
      return <IdCard className="size-4 text-primary" />;
    case 'LayoutDashboard':
      return <LayoutDashboard className="size-4 text-primary" />;
    case 'LibraryBig':
      return <LibraryBig className="size-4 text-primary" />;
    case 'List':
      return <List className="size-4 text-primary" />;
    case 'ListChecks':
      return <ListChecks className="size-4 text-primary" />;
    case 'ListTree':
      return <ListTree className="size-4 text-primary" />;
    case 'Mail':
      return <Mail className="size-4 text-primary" />;
    case 'MailOpen':
      return <MailOpen className="size-4 text-primary" />;
    case 'Megaphone':
      return <Megaphone className="size-4 text-primary" />;
    case 'MenuIcon':
      return <MenuIcon className="size-4 text-primary" />;
    case 'MessagesSquare':
      return <MessagesSquare className="size-4 text-primary" />;
    case 'Newspaper':
      return <Newspaper className="size-4 text-primary" />;
    case 'Package':
      return <Package className="size-4 text-primary" />;
    case 'PackageOpen':
      return <PackageOpen className="size-4 text-primary" />;
    case 'Palette':
      return <Palette className="size-4 text-primary" />;
    case 'Percent':
      return <Percent className="size-4 text-primary" />;
    case 'PhoneCall':
      return <PhoneCall className="size-4 text-primary" />;
    case 'Plus':
      return <Plus className="size-4 text-primary" />;
    case 'PlusCircle':
      return <PlusCircle className="size-4 text-primary" />;
    case 'PlusSquare':
      return <PlusSquare className="size-4 text-primary" />;
    case 'RefreshCw':
      return <RefreshCw className="size-4 text-primary" />;
    case 'ScrollText':
      return <ScrollText className="size-4 text-primary" />;
    case 'Send':
      return <Send className="size-4 text-primary" />;
    case 'Settings':
      return <Settings className="size-4 text-primary" />;
    case 'Shield':
      return <Shield className="size-4 text-primary" />;
    case 'ShieldCheck':
      return <ShieldCheck className="size-4 text-primary" />;
    case 'Sparkles':
      return <Sparkles className="size-4 text-primary" />;
    case 'SquareLibrary':
      return <SquareLibrary className="size-4 text-primary" />;
    case 'Tag':
      return <Tag className="size-4 text-primary" />;
    case 'Tags':
      return <Tags className="size-4 text-primary" />;
    case 'TicketPercent':
      return <TicketPercent className="size-4 text-primary" />;
    case 'TrendingUp':
      return <TrendingUp className="size-4 text-primary" />;
    case 'Trophy':
      return <Trophy className="size-4 text-primary" />;
    case 'Upload':
      return <Upload className="size-4 text-primary" />;
    case 'UserCheck':
      return <UserCheck className="size-4 text-primary" />;
    case 'UserCircle':
      return <UserCircle className="size-4 text-primary" />;
    case 'UserMinus':
      return <UserMinus className="size-4 text-primary" />;
    case 'UserPlus':
      return <UserPlus className="size-4 text-primary" />;
    case 'Users':
      return <Users className="size-4 text-primary" />;
    case 'UserX':
      return <UserX className="size-4 text-primary" />;
    case 'Video':
      return <Video className="size-4 text-primary" />;
    case 'Wallet':
      return <Wallet className="size-4 text-primary" />;
    default:
      return null;
  }
};

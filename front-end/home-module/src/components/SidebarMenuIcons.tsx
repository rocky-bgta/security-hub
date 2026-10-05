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
      return (
        <Activity className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Archive':
      return (
        <Archive className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Award':
      return <Award className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'BarChart':
      return (
        <BarChart className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'BarChart3':
      return (
        <BarChart3 className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Bell':
      return <Bell className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'BookCheck':
      return (
        <BookCheck className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'BookmarkCheck':
      return (
        <BookmarkCheck
          className="home-size-5 home-shrink-0"
          aria-hidden="true"
        />
      );
    case 'BookOpen':
      return (
        <BookOpen className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Boxes':
      return <Boxes className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Brain':
      return <Brain className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Bug':
      return <Bug className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Building2':
      return (
        <Building2 className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Calendar':
      return (
        <Calendar className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'ChevronRight':
      return (
        <ChevronRight
          className="home-size-5 home-shrink-0"
          aria-hidden="true"
        />
      );
    case 'Clock':
      return <Clock className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Cog':
      return <Cog className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'CreditCard':
      return (
        <CreditCard className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Database':
      return (
        <Database className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'DollarSign':
      return (
        <DollarSign className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'FileCheck':
      return (
        <FileCheck className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'FileImage':
      return (
        <FileImage className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'FilePlus':
      return (
        <FilePlus className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'FileText':
      return (
        <FileText className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'GraduationCap':
      return (
        <GraduationCap
          className="home-size-5 home-shrink-0"
          aria-hidden="true"
        />
      );
    case 'Globe':
      return <Globe className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Grid3X3':
      return (
        <Grid3X3 className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Headset':
      return (
        <Headset className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'History':
      return (
        <History className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'IdCard':
      return (
        <IdCard className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'LayoutDashboard':
      return (
        <LayoutDashboard
          className="home-size-5 home-shrink-0"
          aria-hidden="true"
        />
      );
    case 'LibraryBig':
      return (
        <LibraryBig className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'List':
      return <List className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'ListChecks':
      return (
        <ListChecks className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'ListTree':
      return (
        <ListTree className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Mail':
      return <Mail className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'MailOpen':
      return (
        <MailOpen className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Megaphone':
      return (
        <Megaphone className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'MenuIcon':
      return (
        <MenuIcon className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'MessagesSquare':
      return (
        <MessagesSquare
          className="home-size-5 home-shrink-0"
          aria-hidden="true"
        />
      );
    case 'Newspaper':
      return (
        <Newspaper className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Package':
      return (
        <Package className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'PackageOpen':
      return (
        <PackageOpen className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Palette':
      return (
        <Palette className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Percent':
      return (
        <Percent className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'PhoneCall':
      return (
        <PhoneCall className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Plus':
      return <Plus className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'PlusCircle':
      return (
        <PlusCircle className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'PlusSquare':
      return (
        <PlusSquare className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'RefreshCw':
      return (
        <RefreshCw className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'ScrollText':
      return (
        <ScrollText className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Send':
      return <Send className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Settings':
      return (
        <Settings className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Shield':
      return (
        <Shield className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'ShieldCheck':
      return (
        <ShieldCheck className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Sparkles':
      return (
        <Sparkles className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'SquareLibrary':
      return (
        <SquareLibrary
          className="home-size-5 home-shrink-0"
          aria-hidden="true"
        />
      );
    case 'Tag':
      return <Tag className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Tags':
      return <Tags className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'TicketPercent':
      return (
        <TicketPercent
          className="home-size-5 home-shrink-0"
          aria-hidden="true"
        />
      );
    case 'TrendingUp':
      return (
        <TrendingUp className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Trophy':
      return (
        <Trophy className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Upload':
      return (
        <Upload className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'UserCheck':
      return (
        <UserCheck className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'UserCircle':
      return (
        <UserCircle className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'UserMinus':
      return (
        <UserMinus className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'UserPlus':
      return (
        <UserPlus className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    case 'Users':
      return <Users className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'UserX':
      return <UserX className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Video':
      return <Video className="home-size-5 home-shrink-0" aria-hidden="true" />;
    case 'Wallet':
      return (
        <Wallet className="home-size-5 home-shrink-0" aria-hidden="true" />
      );
    default:
      return null;
  }
};

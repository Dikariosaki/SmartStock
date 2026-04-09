export interface MenuItem {
  id: string;
  title: string;
  description: string;
  icon: string;
  route: string;
  isActive: boolean;
  order: number;
}

export interface UserProfile {
  id: number;
  name: string;
  identification: string;
  email: string;
  phone: string;
  role: string;
}

export interface MenuSection {
  title: string;
  items: MenuItem[];
}

export interface MenuConfig {
  sections: MenuSection[];
  userProfile: UserProfile;
}

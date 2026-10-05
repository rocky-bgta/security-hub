import { useEffect, useRef, useState } from 'react';
import { useLocation } from 'react-router-dom';

import { BarIcon, MenuProfileIcon, SearchIcon } from 'assets/icons';
import { GrCaretDown } from 'react-icons/gr';
import { IoMdNotificationsOutline } from 'react-icons/io';
import { IoHelpCircleOutline, IoLogOutOutline } from 'react-icons/io5';

import { Button } from 'common/Button';
import { Input } from 'common/Input';

import Logo from '../assets/images/logo.svg';
import ProfileImage from '../assets/images/profile.jpg';
import LanguageFlag from '../assets/images/usa-flag.png';

const Navbar = ({
  setShowSidebar,
}: {
  setShowSidebar: (showSidebar: boolean | ((prev: boolean) => boolean)) => void;
}) => {
  const location = useLocation();
  const dropdownRef = useRef<HTMLDivElement>(null);
  const [dropdownVisible, setDropdownVisible] = useState<boolean>(false);
  const [showSearch, setShowSearch] = useState<boolean>(false);

  useEffect(() => {
    setShowSearch(false);
  }, [location.pathname]);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setDropdownVisible(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, []);

  const toggleDropdown = () => {
    setDropdownVisible(true);
  };

  const toggleSidebar = () => {
    setShowSidebar((prev: boolean) => !prev);
  };

  return (
    <div className="content-h-16 content-bg-dark content-px-6 content-py-3">
      <div className="content-flex content-items-center content-justify-between">
        <div className="content-flex content-items-center content-gap-8">
          <button onClick={toggleSidebar}>
            <BarIcon />
          </button>
          <a href="#">
            <img src={Logo} alt="Logo" />
          </a>
        </div>
        <div className="content-flex content-items-center content-gap-6">
          <div>
            {showSearch ? (
              <div className="content-relative content-w-96">
                <Input
                  id="search"
                  type="text"
                  placeholder="Search"
                  className="content-rounded-full content-border content-border-graphite content-bg-transparent content-py-2 content-pl-4 content-pr-14 content-text-sm content-text-white"
                />
                <div className="content-absolute content-right-4 content-top-1/2 -content-translate-y-1/2 content-transform content-cursor-pointer content-border-l content-border-ash-gray content-pl-4">
                  <SearchIcon
                    onClick={() => setShowSearch(true)}
                    width={20}
                    height={20}
                    stroke="#13cd9c"
                  />
                </div>
              </div>
            ) : (
              <div className="content-cursor-pointer">
                <SearchIcon
                  onClick={() => setShowSearch(true)}
                  width={20}
                  height={20}
                  stroke="#9a9a9a"
                />
              </div>
            )}
          </div>
          <IoMdNotificationsOutline className="content-text-2xl content-text-ash-gray" />
          <div>
            <img
              className="content-size-7 content-rounded-full"
              src={LanguageFlag}
              alt="Language"
            />
          </div>
          <div onClick={toggleDropdown} ref={dropdownRef}>
            <div className="content-flex content-cursor-pointer content-items-center content-gap-1.5">
              <img
                src={ProfileImage}
                alt="Profile Image"
                className="content-size-9 content-rounded-full"
              />
              <p className="content-text-sm content-text-ash-gray">
                Super Admin
              </p>
              <GrCaretDown className="content-text-xs content-text-ash-gray" />
            </div>
            {dropdownVisible && (
              <div className="content-shadow-soft-shadow content-absolute content-right-5 content-top-16 content-z-[51] content-w-80 content-rounded-b content-bg-dark content-p-2">
                <div className="content-flex content-items-center content-gap-5 content-rounded-lg content-bg-[#42556740] content-p-3">
                  <img
                    className="content-size-14 content-rounded-full"
                    src={ProfileImage}
                    alt="Profile Image"
                  />
                  <div>
                    <h3 className="content-font-semibold content-text-white">
                      Adam Tanjil
                    </h3>
                    <p className="content-text-cloudy-white">
                      tanjil@aspiretss.com
                    </p>
                  </div>
                </div>

                <div className="content-mt-4 content-flex content-flex-col">
                  <Button className="content-flex content-items-center content-gap-4 content-text-white hover:content-bg-ash-gray">
                    <MenuProfileIcon /> Profile
                  </Button>
                  <Button className="content-flex content-items-center content-gap-4 content-text-white hover:content-bg-ash-gray">
                    <IoHelpCircleOutline className="content-text-2xl" /> Help
                  </Button>
                  <Button className="content-flex content-items-center content-gap-4 content-text-white hover:content-bg-ash-gray">
                    <IoLogOutOutline className="content-text-2xl" /> Log out
                  </Button>
                </div>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Navbar;
